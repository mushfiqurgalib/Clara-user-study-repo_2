#include "libavutil/intreadwrite.h"
#include "avformat.h"

#define JV_PREAMBLE_SIZE 5

typedef struct {
int audio_size;    /** audio packet size (bytes) */
int video_size;    /** video packet size (bytes) */
int palette_size;  /** palette size (bytes) */
int video_type;    /** per-frame video compression type */
} JVFrame;

typedef struct {
JVFrame *frames;
enum {
JV_AUDIO = 0,
JV_VIDEO,
JV_PADDING
} state;
int64_t pts;
} JVDemuxContext;

#define MAGIC " Compression by John M Phillips Copyright (C) 1995 The Bitmap Brothers Ltd."

static int read_probe(AVProbeData *pd)
{
if (pd->buf[0] == 'J' && pd->buf[1] == 'V' &&
!memcmp(pd->buf + 4, MAGIC, FFMIN(strlen(MAGIC), pd->buf_size - 4)))
return AVPROBE_SCORE_MAX;
return 0;
}

static int read_header(AVFormatContext *s,
AVFormatParameters *ap)
{
JVDemuxContext *jv = s->priv_data;
AVIOContext *pb = s->pb;
AVStream *vst, *ast;
int64_t audio_pts = 0;
int64_t offset;
int i;

avio_skip(pb, 80);

ast = av_new_stream(s, 0);
vst = av_new_stream(s, 1);
if (!ast || !vst)
return AVERROR(ENOMEM);

vst->codec->codec_type  = CODEC_TYPE_VIDEO;
vst->codec->codec_id    = CODEC_ID_JV;
vst->codec->codec_tag   = 0; /* no fourcc */
vst->codec->width       = avio_rl16(pb);
vst->codec->height      = avio_rl16(pb);
vst->nb_frames          =
ast->nb_index_entries   = avio_rl16(pb);
av_set_pts_info(vst, 64, avio_rl16(pb), 1000);

avio_skip(pb, 4);

ast->codec->codec_type  = CODEC_TYPE_AUDIO;
ast->codec->codec_id    = CODEC_ID_PCM_U8;
ast->codec->codec_tag   = 0; /* no fourcc */
ast->codec->sample_rate = avio_rl16(pb);
ast->codec->channels    = 1;
av_set_pts_info(ast, 64, 1, ast->codec->sample_rate);

avio_skip(pb, 10);

ast->index_entries = av_malloc(ast->nb_index_entries * sizeof(*ast->index_entries));
if (!ast->index_entries)
return AVERROR(ENOMEM);

jv->frames = av_malloc(ast->nb_index_entries * sizeof(JVFrame));
if (!jv->frames)
return AVERROR(ENOMEM);

offset = 0x68 + ast->nb_index_entries * 16;
for(i = 0; i < ast->nb_index_entries; i++) {
AVIndexEntry *e   = ast->index_entries + i;
JVFrame      *jvf = jv->frames + i;

/* total frame size including audio, video, palette data and padding */
e->size         = avio_rl32(pb);
e->timestamp    = i;
e->pos          = offset;
offset         += e->size;

jvf->audio_size = avio_rl32(pb);
jvf->video_size = avio_rl32(pb);
jvf->palette_size = avio_r8(pb) ? 768 : 0;
jvf->video_size = FFMIN(FFMAX(jvf->video_size, 0),
INT_MAX - JV_PREAMBLE_SIZE - jvf->palette_size);
if (avio_r8(pb))
av_log(s, AV_LOG_WARNING, "unsupported audio codec\n");
jvf->video_type = avio_r8(pb);
avio_skip(pb, 1);

e->timestamp = jvf->audio_size ? audio_pts : AV_NOPTS_VALUE;
audio_pts += jvf->audio_size;

e->flags = jvf->video_type != 1 ? AVINDEX_KEYFRAME : 0;
}

jv->state = JV_AUDIO;
return 0;
}

static int read_packet(AVFormatContext *s, AVPacket *pkt)
{
JVDemuxContext *jv = s->priv_data;
AVIOContext *pb = s->pb;
AVStream *ast = s->streams[0];

    while (!url_feof(s->pb) && jv->pts < ast->nb_index_entries) {
    while (!s->pb->eof_reached && jv->pts < ast->nb_index_entries) {
const AVIndexEntry *e   = ast->index_entries + jv->pts;
const JVFrame      *jvf = jv->frames + jv->pts;

switch(jv->state) {
case JV_AUDIO:
jv->state++;
if (jvf->audio_size ) {
if (av_get_packet(s->pb, pkt, jvf->audio_size) < 0)
return AVERROR(ENOMEM);
pkt->stream_index = 0;
pkt->pts          = e->timestamp;
pkt->flags       |= PKT_FLAG_KEY;
return 0;
}
case JV_VIDEO:
jv->state++;
if (jvf->video_size || jvf->palette_size) {
int size = jvf->video_size + jvf->palette_size;
if (av_new_packet(pkt, size + JV_PREAMBLE_SIZE))
return AVERROR(ENOMEM);

AV_WL32(pkt->data, jvf->video_size);
pkt->data[4]      = jvf->video_type;
if (avio_read(pb, pkt->data + JV_PREAMBLE_SIZE, size) < 0)
return AVERROR(EIO);

pkt->size         = size + JV_PREAMBLE_SIZE;
pkt->stream_index = 1;
pkt->pts          = jv->pts;
if (jvf->video_type != 1)
pkt->flags |= PKT_FLAG_KEY;
return 0;
}
case JV_PADDING:
avio_skip(pb, FFMAX(e->size - jvf->audio_size - jvf->video_size
- jvf->palette_size, 0));
jv->state = JV_AUDIO;
jv->pts++;
}
}

return AVERROR(EIO);
}

static int read_seek(AVFormatContext *s, int stream_index,
int64_t ts, int flags)
{
JVDemuxContext *jv = s->priv_data;
AVStream *ast = s->streams[0];
int i;

if (flags & (AVSEEK_FLAG_BYTE|AVSEEK_FLAG_FRAME))
return AVERROR_NOTSUPP;

switch(stream_index) {
case 0:
i = av_index_search_timestamp(ast, ts, flags);
break;
case 1:
i = ts;
break;
default:
return 0;
}

if (i < 0 || i >= ast->nb_index_entries)
return 0;

jv->state = JV_AUDIO;
jv->pts   = i;
avio_seek(s->pb, ast->index_entries[i].pos, SEEK_SET);
return 0;
}

AVInputFormat ff_jv_demuxer = {
.name           = "jv",
.long_name      = NULL_IF_CONFIG_SMALL("Bitmap Brothers JV"),
.priv_data_size = sizeof(JVDemuxContext),
.read_probe     = read_probe,
.read_header    = read_header,
.read_packet    = read_packet,
.read_seek      = read_seek,
};
