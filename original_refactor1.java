@@ -127,7 +127,7 @@ public class StatisticsUtil {
     public static List<ResultRow> executeQuery(String template, Map<String, String> params) {
         StringSubstitutor stringSubstitutor = new StringSubstitutor(params);
         String sql = stringSubstitutor.replace(template);
-        return execStatisticQuery(sql);
     }
 
     public static void execUpdate(String template, Map<String, String> params) throws Exception {
@@ -137,10 +137,15 @@ public static void execUpdate(String template, Map<String, String> params) throw
     }
 
     public static List<ResultRow> execStatisticQuery(String sql) {
         if (!FeConstants.enableInternalSchemaDb) {
             return Collections.emptyList();
         }
-        try (AutoCloseConnectContext r = StatisticsUtil.buildConnectContext()) {
             if (Config.isCloudMode()) {
                 r.connectContext.getCloudCluster();
             }
@@ -178,10 +183,10 @@ public static List<Histogram> deserializeToHistogramStatistics(List<ResultRow> r
     }
 
     public static AutoCloseConnectContext buildConnectContext() {
-        return buildConnectContext(false);
     }
 
-    public static AutoCloseConnectContext buildConnectContext(boolean limitScan) {
         ConnectContext connectContext = new ConnectContext();
         SessionVariable sessionVariable = connectContext.getSessionVariable();
         sessionVariable.internalSession = true;
@@ -207,7 +212,14 @@ public static AutoCloseConnectContext buildConnectContext(boolean limitScan) {
         connectContext.setQualifiedUser(UserIdentity.ROOT.getQualifiedUser());
         connectContext.setCurrentUserIdentity(UserIdentity.ROOT);
         connectContext.setStartTime();
-        return new AutoCloseConnectContext(connectContext);
     }
 
    
