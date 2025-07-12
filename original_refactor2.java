 public class NereidsSqlCacheManager {
     // value: SqlCacheContext
     private volatile Cache<String, SqlCacheContext> sqlCaches;
 
    public NereidsSqlCacheManager(int sqlCacheNum, long cacheIntervalSeconds) {
        sqlCaches = buildSqlCaches(sqlCacheNum, cacheIntervalSeconds);
     }
 
     public static synchronized void updateConfig() {
public static synchronized void updateConfig() {
 
         Cache<String, SqlCacheContext> sqlCaches = buildSqlCaches(
                 Config.sql_cache_manage_num,
             Config.cache_last_version_interval_second
         );
         sqlCaches.putAll(sqlCacheManager.sqlCaches.asMap());
         sqlCacheManager.sqlCaches = sqlCaches;
     }
 
    private static Cache<String, SqlCacheContext> buildSqlCaches(int sqlCacheNum, long cacheIntervalSeconds) {
        sqlCacheNum = sqlCacheNum < 0 ? 100 : sqlCacheNum;
        cacheIntervalSeconds = cacheIntervalSeconds < 0 ? 30 : cacheIntervalSeconds;

        return Caffeine.newBuilder()
                .maximumSize(sqlCacheNum)
                .expireAfterAccess(Duration.ofSeconds(cacheIntervalSeconds))
                 // auto evict cache when jvm memory too low
                .softValues()
                .build();
     }
  public Optional<LogicalSqlCache> tryParseSql(ConnectContext connectContext, Stri
     }
 
     private boolean tablesOrDataChanged(Env env, SqlCacheContext sqlCacheContext) {
        long latestPartitionTime = sqlCacheContext.getLatestPartitionTime();
        long latestPartitionVersion = sqlCacheContext.getLatestPartitionVersion();

         if (sqlCacheContext.hasUnsupportedTables()) {
             return true;
         }
 private boolean tablesOrDataChanged(Env env, SqlCacheContext sqlCacheContext) {
             long cacheTableTime = scanTable.latestTimestamp;
             long currentTableVersion = olapTable.getVisibleVersion();
             long cacheTableVersion = scanTable.latestVersion;
          // some partitions have been dropped, or delete or update or insert rows into new partition?
             if (currentTableTime > cacheTableTime
                     || (currentTableTime == cacheTableTime && currentTableVersion > cacheTableVersion)) {
                 return true;
 private boolean tablesOrDataChanged(Env env, SqlCacheContext sqlCacheContext) {
             for (Long scanPartitionId: scanTable.getScanPartitions()) {
                 Partition partition = olapTable.getPartition(scanPartitionId);
                 // partition == null: is this partition truncated?
               if (partition == null || partition.getVisibleVersionTime() > latestPartitionTime
                        || (partition.getVisibleVersionTime() == latestPartitionTime
                        && partition.getVisibleVersion() > latestPartitionVersion)) {
                     return true;
                 }
             }
