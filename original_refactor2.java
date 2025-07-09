@@ -17,6 +17,7 @@
 
 package org.apache.doris.udf;
 
 import org.apache.doris.catalog.PrimitiveType;
 import org.apache.doris.catalog.ScalarType;
 import org.apache.doris.catalog.Type;
@@ -25,22 +26,28 @@
 import org.apache.doris.thrift.TScalarType;
 import org.apache.doris.thrift.TTypeDesc;
 import org.apache.doris.thrift.TTypeNode;
-import org.apache.doris.udf.UdfExecutor.JavaUdfDataType;
 
 import com.google.common.base.Preconditions;
 import sun.misc.Unsafe;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 
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
         // Check if the evaluate method return type is compatible with the return type from
         // the function definition. This happens when both of them map to the same primitive
         // type.
-        if (retType.getPrimitiveType().toThrift() != javaType.getPrimitiveType()) {
-            return Pair.of(false, javaType);
         }
-        return Pair.of(true, javaType);
     }
 
     /**
@@ -154,18 +250,48 @@ public static Pair<Boolean, JavaUdfDataType[]> setArgTypes(Type[] parameterTypes
         JavaUdfDataType[] inputArgTypes = new JavaUdfDataType[parameterTypes.length];
         int firstPos = isUdaf ? 1 : 0;
         for (int i = 0; i < parameterTypes.length; ++i) {
-            inputArgTypes[i] = JavaUdfDataType.getType(udfArgTypes[i + firstPos]);
-            if (inputArgTypes[i].getPrimitiveType() != parameterTypes[i].getPrimitiveType().toThrift()) {
                 return Pair.of(false, inputArgTypes);
             }
         }
         return Pair.of(true, inputArgTypes);
     }
 
     /**
      * input is a 64bit num from backend, and then get year, month, day, hour, minus, second by the order of bits.
      */
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
 
-    /**
-     * a 64bit num convertToDate.
-     */
-    public static LocalDate convertToDate(long date) {
         int year = (int) (date >> 48);
         int yearMonth = (int) (date >> 40);
         int yearMonthDay = (int) (date >> 32);
 
         int month = (yearMonth & 0XFF);
         int day = (yearMonthDay & 0XFF);
-        LocalDate value = LocalDate.of(year, month, day);
         return value;
     }
 
     /**
      * input is the second, minute, hours, day , month and year respectively.
      * and then combining all num to a 64bit value return to backend;
      */
-    public static long convertDateTimeToLong(int year, int month, int day, int hour, int minute, int second,
             boolean isDate) {
         long time = 0;
         time = time + year;
@@ -219,6 +465,48 @@ public static long convertDateTimeToLong(int year, int month, int day, int hour,
         return time;
     }
 
     /**
      * Change the order of the bytes, Because JVM is Big-Endian , x86 is Little-Endian.
      */
