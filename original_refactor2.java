public class UdfUtils {
     public static final Unsafe UNSAFE;
     private static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
     public static final long BYTE_ARRAY_OFFSET;
@@ -59,6 +66,93 @@ public class UdfUtils {
         BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
     }
 
     protected static Pair<Type, Integer> fromThrift(TTypeDesc typeDesc, int nodeIdx) throws InternalException {
         TTypeNode node = typeDesc.getTypes().get(nodeIdx);
         Type type = null;
@@ -134,14 +228,16 @@ public static Pair<Boolean, JavaUdfDataType> setReturnType(Type retType, Class<?
         if (!JavaUdfDataType.isSupported(retType)) {
             throw new InternalException("Unsupported return type: " + retType.toSql());
         }
-        JavaUdfDataType javaType = JavaUdfDataType.getType(udfReturnType);
-        if (retType.getPrimitiveType().toThrift() != javaType.getPrimitiveType()) {
-            return Pair.of(false, javaType);
         }
-        return Pair.of(true, javaType);
     }
 
-    public static LocalDateTime convertToDateTime(long date) {
         int year = (int) (date >> 48);
         int yearMonth = (int) (date >> 40);
         int yearMonthDay = (int) (date >> 32);
@@ -181,29 +307,149 @@ public static LocalDateTime convertToDateTime(long date) {
         int second = (minuteTypeNeg >> 4);
         //here don't need those bits are type = ((minus_type_neg >> 1) & 0x7);
 
-        LocalDateTime value = LocalDateTime.of(year, month, day, hour, minute, second);
         return value;
     }
 

-    public static LocalDate convertToDate(long date) {
         int year = (int) (date >> 48);
         int yearMonth = (int) (date >> 40);
         int yearMonthDay = (int) (date >> 32);
 
         int month = (yearMonth & 0XFF);
         int day = (yearMonthDay & 0XFF);
-        LocalDate value = LocalDate.of(year, month, day);
         return value;
     }
 
 
-    public static long convertDateTimeToLong(int year, int month, int day, int hour, int minute, int second,
             boolean isDate) {
         long time = 0;
         time = time + year;
@@ -219,6 +465,48 @@ public static long convertDateTimeToLong(int year, int month, int day, int hour,
         return time;
     }
 
