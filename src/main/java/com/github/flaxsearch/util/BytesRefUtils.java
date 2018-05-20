package com.github.flaxsearch.util;

/*
 *   Copyright (c) 2016 Lemur Consulting Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Locale;
import java.util.function.Function;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;


public class BytesRefUtils {

    public static final byte SHIFT_START_LONG = 0x20;

    public static final int BUF_SIZE_LONG = 63/7 + 2;

    public static int getPrefixCodedLongShift(final BytesRef val) {
        final int shift = val.bytes[val.offset] - SHIFT_START_LONG;
        if (shift > 63 || shift < 0)
            throw new NumberFormatException("Invalid shift value (" + shift + ") in prefixCoded bytes (is encoded value really an INT?)");
        return shift;
    }

    public static long prefixCodedToLong(final BytesRef val) {
        long sortableBits = 0L;
        for (int i=val.offset+1, limit=val.offset+val.length; i<limit; i++) {
            sortableBits <<= 7;
            final byte b = val.bytes[i];
            if (b < 0) {
                throw new NumberFormatException(
                        "Invalid prefixCoded numerical value representation (byte "+
                                Integer.toHexString(b&0xff)+" at position "+(i-val.offset)+" is invalid)"
                );
            }
            sortableBits |= b;
        }
        return (sortableBits << getPrefixCodedLongShift(val)) ^ 0x8000000000000000L;
    }

    public static void longToPrefixCoded(final long val, final int shift, final BytesRefBuilder bytes) {
        // ensure shift is 0..63
        if ((shift & ~0x3f) != 0) {
            throw new IllegalArgumentException("Illegal shift value, must be 0..63; got shift=" + shift);
        }
        int nChars = (((63-shift)*37)>>8) + 1;    // i/7 is the same as (i*37)>>8 for i in 0..63
        bytes.setLength(nChars+1);   // one extra for the byte that contains the shift info
        bytes.grow(BUF_SIZE_LONG);
        bytes.setByteAt(0, (byte)(SHIFT_START_LONG + shift));
        long sortableBits = val ^ 0x8000000000000000L;
        sortableBits >>>= shift;
        while (nChars > 0) {
            // Store 7 bits per byte for compatibility
            // with UTF-8 encoding of terms
            bytes.setByteAt(nChars--, (byte)(sortableBits & 0x7f));
            sortableBits >>>= 7;
        }
    }

    public static String encode(BytesRef data, String encoding) {
        Function<BytesRef, String> encoder = getEncoder(encoding);
        return encoder.apply(data);
    }

    public static BytesRef decode(String data, String encoding) {
        Function<String, BytesRef> decoder = getDecoder(encoding);
        return decoder.apply(data);
    }

    private static Function<String, BytesRef> getDecoder(String type) {
        switch (type.toLowerCase(Locale.ROOT)) {
            case "base64" :
                return s -> new BytesRef(Base64.getUrlDecoder().decode(s.getBytes(Charset.defaultCharset())));
            case "utf8" :
                return BytesRef::new;
            case "int" :
                return s -> {
                    BytesRefBuilder builder = new BytesRefBuilder();
                    // LegacyNumericUtils.intToPrefixCoded(Integer.parseInt(s), 0, builder);
                    return builder.get();
                };
            case "long" :
                return s -> {
                    BytesRefBuilder builder = new BytesRefBuilder();
                    longToPrefixCoded(Long.parseLong(s), 0, builder);
                    return builder.get();
                };
            case "float" :
                return s -> {
                    BytesRefBuilder builder = new BytesRefBuilder();
                    // LegacyNumericUtils.intToPrefixCoded(NumericUtils.floatToSortableInt(Float.parseFloat(s)), 0, builder);
                    return builder.get();
                };
            case "double" :
                return s -> {
                    BytesRefBuilder builder = new BytesRefBuilder();
                    //LegacyNumericUtils.longToPrefixCoded(NumericUtils.doubleToSortableLong(Double.parseDouble(s)), 0, builder);
                    return builder.get();
                };
            default :
                throw new IllegalArgumentException("Unknown decoder type: " + type);
        }
    }

    private static Function<BytesRef, String> getEncoder(String type) {
        switch (type.toLowerCase(Locale.ROOT)) {
            case "base64" :
                return b -> {
                	BytesRef b2 = BytesRef.deepCopyOf(b);
                	return new String(Base64.getUrlEncoder().encode(b2.bytes), Charset.defaultCharset());
                };
            case "utf8" :
                return BytesRef::utf8ToString;
            case "int" :
                // return b -> Integer.toString(LegacyNumericUtils.prefixCodedToInt(b));
            case "long" :
                return b -> Long.toString(prefixCodedToLong(b));
            case "float" :
                // return b -> Float.toString(NumericUtils.sortableIntToFloat(LegacyNumericUtils.prefixCodedToInt(b)));
            case "double" :
                // return b -> Double.toString(NumericUtils.sortableLongToDouble(LegacyNumericUtils.prefixCodedToLong(b)));
            default:
                throw new IllegalArgumentException("Unknown encoder type: " + type);
        }
    }

}
