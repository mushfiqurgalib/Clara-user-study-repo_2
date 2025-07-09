@@ -60,14 +60,14 @@ public class SparkRepository {
     public static final String REPOSITORY_DIR = "__spark_repository__";
     public static final String PREFIX_ARCHIVE = "__archive_";
     public static final String PREFIX_LIB = "__lib_";
-    public static final String SPARK_DPP = "spark-dpp";
     public static final String SPARK_2X = "spark-2x";
-    public static final String SUFFIX = ".zip";
 
     private static final String PATH_DELIMITER = "/";
     private static final String FILE_NAME_SEPARATOR = "_";
 
-    private static final String DPP_RESOURCE = "/spark-dpp/spark-dpp.jar";
     private static final String SPARK_RESOURCE = "/jars/spark-2x.zip";
 
     private String remoteRepositoryPath;
@@ -85,7 +85,7 @@ public SparkRepository(String remoteRepositoryPath, BrokerDesc brokerDesc) {
         this.brokerDesc = brokerDesc;
         this.currentDppVersion = Config.spark_dpp_version;
         this.currentArchive = new SparkArchive(getRemoteArchivePath(currentDppVersion), currentDppVersion);
-        this.localDppPath = PaloFe.DORIS_HOME_DIR + DPP_RESOURCE;
         if (!Strings.isNullOrEmpty(Config.spark_resource_path)) {
             this.localSpark2xPath = Config.spark_resource_path;
         } else {
@@ -98,7 +98,7 @@ public void prepare() throws LoadException {
     }
 
     private void initRepository() throws LoadException {
-        LOG.info("start to init remote repository");
         boolean needUpload = false;
         boolean needReplace = false;
         CHECK: {
@@ -222,7 +222,11 @@ private void getLibraries(String remoteArchivePath, List<SparkLibrary> libraries
             if (!fileName.startsWith(PREFIX_LIB)) {
                 continue;
             }
-            String[] lib_arg = unWrap(PREFIX_LIB, SUFFIX, fileName).split(FILE_NAME_SEPARATOR);
             if (lib_arg.length != 2) {
                 continue;
             }
@@ -232,16 +236,12 @@ private void getLibraries(String remoteArchivePath, List<SparkLibrary> libraries
             }
             String type = lib_arg[1];
             SparkLibrary.LibType libType = null;
-            switch (type) {
-                case SPARK_DPP:
-                    libType = SparkLibrary.LibType.DPP;
-                    break;
-                case SPARK_2X:
-                    libType = SparkLibrary.LibType.SPARK2X;
-                    break;
-                default:
-                    Preconditions.checkState(false, "wrong library type: " + type);
-                    break;
             }
             SparkLibrary remoteFile = new SparkLibrary(fileStatus.path, md5sum, libType, fileStatus.size);
             libraries.add(remoteFile);
@@ -259,7 +259,7 @@ public String getMd5String(String filePath) throws LoadException {
             LOG.debug("get md5sum from file {}, md5sum={}", filePath, md5sum);
             return md5sum;
         } catch (FileNotFoundException e) {
-            throw new LoadException("file " + filePath + "dose not exist");
         } catch (IOException e) {
             throw new LoadException("failed to get md5sum from file " + filePath);
         }
@@ -302,8 +302,11 @@ private static String getFileName(String delimiter, String path) {
         return path.substring(path.lastIndexOf(delimiter) + 1);
     }
 
-    private static String unWrap(String prefix, String suffix, String fileName) {
-        return fileName.substring(prefix.length(), fileName.length() - suffix.length());
     }
 
     private static String joinPrefix(String prefix, String fileName) {
