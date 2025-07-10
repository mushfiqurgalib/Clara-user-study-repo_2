
@@ -114,20 +113,9 @@ public static DateLiteral getRangeEnd(DateLiteral beginTime, FunctionIntervalInf
         return null;
     }

-    public static Map<String, AddPartitionClause> getNonExistPartitionAddClause(OlapTable olapTable,
-            ArrayList<TStringLiteral> partitionValues, PartitionInfo partitionInfo, ArrayList<Long> existPartitionIds)
             throws AnalysisException {
-        Preconditions.checkArgument(!partitionInfo.isMultiColumnPartition(),
-                "now dont support multi key columns in auto-partition.");
-
         Map<String, AddPartitionClause> result = Maps.newHashMap();
         ArrayList<Expr> partitionExprs = partitionInfo.getPartitionExprs();
         PartitionType partitionType = partitionInfo.getType();
@@ -144,14 +132,6 @@ public static Map<String, AddPartitionClause> getNonExistPartitionAddClause(Olap
                 continue;
             }
             filterPartitionValues.add(value);
-
-
-            Long id = partitionInfo.contains(partitionValue, partitionType);
-            if (id != null) { // found
-                existPartitionIds.add(id);
-                continue;
-            }
-
             if (partitionType == PartitionType.RANGE) {
                 String beginTime = value;
                 DateLiteral beginDateTime = new DateLiteral(beginTime, partitionColumnType);
@@ -167,24 +147,21 @@ public static Map<String, AddPartitionClause> getNonExistPartitionAddClause(Olap
                 listValues.add(Collections.singletonList(lowerValue));
                 partitionKeyDesc = PartitionKeyDesc.createIn(
                         listValues);
-
                 partitionName += getFormatPartitionValue(lowerValue.getStringValue());
                 if (partitionColumnType.isStringType()) {
                     partitionName += "_" + System.currentTimeMillis();
                 }
             } else {
-                throw new AnalysisException("auto-partition only support range and list partition");
             }

             Map<String, String> partitionProperties = Maps.newHashMap();
             DistributionDesc distributionDesc = olapTable.getDefaultDistributionInfo().toDistributionDesc();

-            SinglePartitionDesc partitionDesc = new SinglePartitionDesc(true, partitionName,
                     partitionKeyDesc, partitionProperties);

-            AddPartitionClause addPartitionClause = new AddPartitionClause(partitionDesc,
                     distributionDesc, partitionProperties, false);
             result.put(partitionName, addPartitionClause);
         }
