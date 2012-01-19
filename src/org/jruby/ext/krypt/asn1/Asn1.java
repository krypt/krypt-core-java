/***** BEGIN LICENSE BLOCK *****
* Version: CPL 1.0/GPL 2.0/LGPL 2.1
*
* The contents of this file are subject to the Common Public
* License Version 1.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.eclipse.org/legal/cpl-v10.html
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.
*
* Copyright (C) 2011 
* Hiroshi Nakamura <nahi@ruby-lang.org>
* Martin Bosslet <Martin.Bosslet@googlemail.com>
*
* Alternatively, the contents of this file may be used under the terms of
* either of the GNU General Public License Version 2 or later (the "GPL"),
* or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
* in which case the provisions of the GPL or the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of either the GPL or the LGPL, and not to allow others to
* use your version of this file under the terms of the CPL, indicate your
* decision by deleting the provisions above and replace them with the notice
* and other provisions required by the GPL or the LGPL. If you do not delete
* the provisions above, a recipient may use your version of this file under
* the terms of any one of the CPL, the GPL or the LGPL.
 */
package org.jruby.ext.krypt.asn1;

import impl.krypt.asn1.Asn1Object;
import impl.krypt.asn1.EncodableHeader;
import impl.krypt.asn1.Length;
import impl.krypt.asn1.ParsedHeader;
import impl.krypt.asn1.ParserFactory;
import impl.krypt.asn1.Tag;
import impl.krypt.asn1.TagClass;
import impl.krypt.asn1.Tags;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyNumeric;
import org.jruby.RubyObject;
import org.jruby.RubySymbol;
import org.jruby.anno.JRubyMethod;
import org.jruby.ext.krypt.Errors;
import org.jruby.ext.krypt.Streams;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1BitString;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1BmpString;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Boolean;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Constructive;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1EndOfContents;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Enumerated;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1GeneralString;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1GeneralizedTime;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1GraphicString;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Ia5String;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Integer;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Iso64String;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Null;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1NumericString;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1ObjectId;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1OctetString;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Primitive;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1PrintableString;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Sequence;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Set;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1T61String;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1UniversalString;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1UtcTime;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1Utf8String;
import org.jruby.ext.krypt.asn1.Asn1DataClasses.Asn1VideotexString;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;

/**
 * 
 * @author <a href="mailto:Martin.Bosslet@googlemail.com">Martin Bosslet</a>
 */
public class Asn1 {
    
    private Asn1() {}
    
    static final impl.krypt.asn1.Parser PARSER = new ParserFactory().newHeaderParser();
    
    public static interface Asn1Codec {
        public byte[] encode(Ruby runtime, IRubyObject value);
        public IRubyObject decode(Ruby runtime, byte[] value);
    }
    
    private static Asn1Codec codecFor(int tag, TagClass tagClass)
    {
        Asn1Codec codec;
        if (tag < 30 && tagClass.equals(TagClass.UNIVERSAL))
            codec = Asn1Codecs.CODECS[tag];
        else
            codec = null;
        codec = null; /* TODO */
        return codec;
    }
    
    public static class Asn1Data extends RubyObject {
        
        private static ObjectAllocator ALLOCATOR = new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass type) {
                return new Asn1Data(runtime, type);
            }
        };
        
        protected Asn1Data(Ruby runtime, RubyClass type) {
            super(runtime, type);
        }
        
        private Asn1Object object;
        private Asn1Codec codec;
        
        private IRubyObject tag;
        private IRubyObject tagClass;
        private IRubyObject infiniteLength;
        private IRubyObject value = null;
        
        static Asn1Data newAsn1Data(Ruby runtime, Asn1Object object) {
            Tag t = object.getHeader().getTag();
            int itag = t.getTag();
            TagClass tc = t.getTagClass();
            RubyClass c = null;
            
            try {
                if (t.isConstructed()) {
                    if (itag < 31 && tc.equals(TagClass.UNIVERSAL))
                        c = (RubyClass)ASN1_INFOS[itag][1];
                    if (c == null || object.getHeader().getLength().isInfiniteLength()) {
                        c = cAsn1Constructive;
                        return new Asn1Constructive(runtime, c, object);
                    } else {
                        return (Asn1Data)((Constructor)ASN1_INFOS[itag][2]).newInstance(runtime, c, object);
                    }
                }
                else {
                    if (itag < 31 && tc.equals(TagClass.UNIVERSAL)) {
                        c = (RubyClass)ASN1_INFOS[itag][1];
                        if (c == null) {
                            c = cAsn1Primitive;
                            return new Asn1Primitive(runtime, c, object);
                        } else {
                            return (Asn1Data)((Constructor)ASN1_INFOS[itag][2]).newInstance(runtime, c, object);
                        }
                    } else {
                        return new Asn1Data(runtime,
                                            cAsn1Data,
                                            object);
                    }
                }
            } catch (Exception ex) {
                throw runtime.newRuntimeError(ex.getMessage());
            }
        }
        
        protected Asn1Object getObject() {
            return object;
        }
        
        protected Asn1Codec getCodec() {
            return codec;
        }
        
        protected Asn1Data(Ruby runtime, 
                         RubyClass type, 
                         Asn1Object object) {
            super(runtime, type);
            if (object == null) throw new NullPointerException();
            this.object = object;
            impl.krypt.asn1.Header h = object.getHeader();
            Tag t = h.getTag();
            int itag = t.getTag();
            TagClass tc = t.getTagClass();
            Length length = h.getLength(); 
            this.tag = runtime.newFixnum(itag);
            this.tagClass = Header.tagClassFor(runtime, tc);
            this.infiniteLength = runtime.newBoolean(length.isInfiniteLength());
            this.codec = codecFor(itag, tc);
        }
        
        @JRubyMethod
        public IRubyObject initialize(ThreadContext ctx, IRubyObject value, IRubyObject tag, IRubyObject tag_class) {
            Ruby runtime = ctx.getRuntime();
            if(!(tag_class instanceof RubySymbol)) {
                throw Errors.newAsn1Error(runtime, "tag_class must be a symbol");
            }
            int itag = RubyNumeric.fix2int(tag);
            TagClass tc = TagClass.valueOf(tag_class.toString());
            boolean isConstructed = value instanceof RubyArray;

            initInternal(this, 
                        itag, 
                        tc, 
                        isConstructed, 
                        false);
            
            this.tag = tag;
            this.tagClass = tag_class;
            this.value = value;
            
            return this;
        }
        
        @JRubyMethod
        public IRubyObject tag() {
            return tag;
        }
        
        @JRubyMethod
        public IRubyObject tag_class() {
            return tagClass;
        }
        
        @JRubyMethod
        public IRubyObject infinite_length() {
            return infiniteLength;
        }
        
        @JRubyMethod
        public synchronized IRubyObject value(ThreadContext ctx) {
            if (value == null) {
                value = decodeValue(ctx);
            }
            return value;
        }
        
        @JRubyMethod(name={"tag="})
        public synchronized IRubyObject set_tag(IRubyObject value) {
            if (tag == value)
                return value;
            int itag = RubyNumeric.fix2int(value);
            Tag t = object.getHeader().getTag();
            t.setTag(itag);
            codec = codecFor(itag, t.getTagClass());
            this.tag = value;
            return value;
        }
        
        @JRubyMethod(name={"tag_class="})
        public synchronized IRubyObject set_tag_class(ThreadContext ctx, IRubyObject value) {
            if (tagClass == value)
                return value;
            if(!(value instanceof RubySymbol))
                throw Errors.newAsn1Error(ctx.getRuntime(), "tag_class must be a symbol");
            TagClass tc = TagClass.valueOf(value.toString());
            Tag t = object.getHeader().getTag();
            t.setTagClass(tc);
            codec = codecFor(t.getTag(), tc);
            this.tagClass = value;
            return value;
        }
        
        @JRubyMethod(name={"infinite_length="})
        public synchronized IRubyObject set_infinite_length(IRubyObject value) {
            boolean boolVal = value.isTrue();
            Length l = object.getHeader().getLength();
            l.setInfiniteLength(boolVal);
            this.infiniteLength = value;
            return value;
        }
        
        @JRubyMethod(name={"value="})
        public synchronized IRubyObject set_value(IRubyObject value) {
            object.getHeader().getLength().setLength(0);
            object.invalidateValue();
            boolean isConstructed = value instanceof RubyArray;
            object.getHeader().getTag().setConstructed(isConstructed);
            this.value = value;
            return value;
        }
        
        @JRubyMethod
        public synchronized IRubyObject encode_to(ThreadContext ctx, IRubyObject io) {
            Ruby rt = ctx.getRuntime();
            OutputStream out = Streams.tryWrapAsOuputStream(rt, io);
            encodeToInternal(ctx, out);
            return this;
        }
        
        @JRubyMethod
        public synchronized IRubyObject to_der(ThreadContext ctx) {
            Ruby rt = ctx.getRuntime();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            encodeToInternal(ctx, baos);
            return rt.newString(new ByteList(baos.toByteArray(), false));
        }
        
        protected void encodeToInternal(ThreadContext ctx, OutputStream out) {
            try {
                byte[] rawValue = object.getValue();
                if (rawValue == null) {
                    Tag t = object.getHeader().getTag();
                    if (t.getTagClass().equals(TagClass.UNIVERSAL) &&
                        (t.getTag() == Tags.NULL || t.getTag() == Tags.END_OF_CONTENTS)) {
                        object.encodeTo(out);
                    } else {
                        encodeTo(ctx, value, out);
                    }
                } else {
                    object.encodeTo(out);
                }
            } catch (IOException ex) {
                throw Errors.newSerializeError(ctx.getRuntime(), ex.getMessage());
            }
        }
        
        protected IRubyObject decodeValue(ThreadContext ctx) {
            if (object.getHeader().getTag().isConstructed()) {
                return Asn1Constructive.decodeValue(ctx, object.getValue());
            } else {
                return Asn1Primitive.decodeValue(ctx.getRuntime(), codec, object.getValue());
            }
        }
        
        protected void encodeTo(ThreadContext ctx, IRubyObject value, OutputStream out) {
            try {
                if (object.getHeader().getTag().isConstructed()) {
                    Asn1Constructive.encodeTo(ctx, object, value, out);
                } else {
                    Asn1Primitive.encodeTo(ctx.getRuntime(), codec, object, value, out);
                }
            } catch (IOException ex) {
                throw Errors.newSerializeError(ctx.getRuntime(), ex.getMessage());
            }
        }
    }
    
    static void initInternal(Asn1Data data,
                              int tag,
                              TagClass tagClass,
                              boolean isConstructed,
                              boolean isInfinite)
    {
        EncodableHeader h = new EncodableHeader(tag, tagClass, isConstructed, isInfinite);
        data.object = new Asn1Object(h, null);
        data.codec = codecFor(tag, tagClass);
    }
    
    static void defaultInitialize(Asn1Data data,
                                          Ruby runtime, 
                                          IRubyObject value, 
                                          IRubyObject tag, 
                                          IRubyObject tag_class, 
                                          boolean isConstructed) {
        if(!(tag_class instanceof RubySymbol)) {
            throw Errors.newAsn1Error(runtime, "tag_class must be a symbol");
        }
        int itag = RubyNumeric.fix2int(tag);
        TagClass tc = TagClass.valueOf(tag_class.toString());
        
        initInternal(data, 
                    itag, 
                    tc, 
                    isConstructed, 
                    false);

        data.tag = tag;
        data.tagClass = tag_class;
        data.value = value;
    }
    
    @JRubyMethod(meta = true)
    public static IRubyObject decode(ThreadContext ctx, IRubyObject recv, IRubyObject value) {
        try {
            Ruby rt = ctx.getRuntime();
            InputStream in;
            if (value.respondsTo("read")) {
                in = Streams.tryWrapAsInputStream(rt, value);
            } else {
                in = new ByteArrayInputStream(toDerIfPossible(value).convertToString().getBytes());
            }
            ParsedHeader h = PARSER.next(in);
            return Asn1Data.newAsn1Data(rt, h.getObject());
        } catch(Exception e) {
            throw Errors.newParseError(ctx.getRuntime(), e.getMessage());
        }
    }
    
    public static IRubyObject toDer(IRubyObject obj) {
        return obj.callMethod(obj.getRuntime().getCurrentContext(), "to_der");
    }

    public static IRubyObject toDerIfPossible(IRubyObject der) {
        if(der.respondsTo("to_der")) {
            return toDer(der);
        } else {
            return der;
        }
    }
    
    private static RubyClass cAsn1Data;
    private static RubyClass cAsn1Primitive;
    private static RubyClass cAsn1Constructive;
    
    private static RubyClass cAsn1EndOfContents;
    private static RubyClass cAsn1Boolean;
    private static RubyClass cAsn1Integer;
    private static RubyClass cAsn1BitString;
    private static RubyClass cAsn1OctetString;
    private static RubyClass cAsn1Null;
    private static RubyClass cAsn1ObjectId;
    private static RubyClass cAsn1Enumerated;
    private static RubyClass cAsn1Utf8String;
    private static RubyClass cAsn1Sequence;
    private static RubyClass cAsn1Set;
    private static RubyClass cAsn1NumericString;
    private static RubyClass cAsn1PrintableString;
    private static RubyClass cAsn1T61String;
    private static RubyClass cAsn1VideotexString;
    private static RubyClass cAsn1Ia5String;
    private static RubyClass cAsn1UtcTime;
    private static RubyClass cAsn1GeneralizedTime;
    private static RubyClass cAsn1GraphicString;
    private static RubyClass cAsn1Iso64String;
    private static RubyClass cAsn1GeneralString;
    private static RubyClass cAsn1UniversalString;
    private static RubyClass cAsn1BmpString;
    
    private static Object[][] ASN1_INFOS;
    
    public static void createAsn1(Ruby runtime, RubyModule krypt, RubyClass kryptError) {
        RubyModule mAsn1 = runtime.defineModuleUnder("Asn1", krypt);

        RubyClass asn1Error = mAsn1.defineClassUnder("Asn1Error", kryptError, kryptError.getAllocator());
        mAsn1.defineClassUnder("ParseError", asn1Error, asn1Error.getAllocator());
        mAsn1.defineClassUnder("SerializeError", asn1Error, asn1Error.getAllocator());

        mAsn1.defineAnnotatedMethods(Asn1.class);
        
        cAsn1Data = mAsn1.defineClassUnder("Asn1Data", runtime.getObject(), Asn1Data.ALLOCATOR);
        cAsn1Data.defineAnnotatedMethods(Asn1Data.class);

        cAsn1Primitive = mAsn1.defineClassUnder("Primitive", cAsn1Data, Asn1Primitive.PRIMITIVE_ALLOCATOR);
        cAsn1Primitive.defineAnnotatedMethods(Asn1Primitive.class);

        cAsn1Constructive = mAsn1.defineClassUnder("Constructive", cAsn1Data, Asn1Constructive.CONSTRUCTIVE_ALLOCATOR);
        cAsn1Constructive.includeModule(runtime.getModule("Enumerable"));
        cAsn1Constructive.defineAnnotatedMethods(Asn1Constructive.class);

        cAsn1EndOfContents   = mAsn1.defineClassUnder("EndOfContents", cAsn1Primitive, Asn1EndOfContents.ALLOCATOR);
        cAsn1EndOfContents.defineAnnotatedMethods(Asn1EndOfContents.class);
        cAsn1Boolean         = mAsn1.defineClassUnder("Boolean", cAsn1Primitive, Asn1Boolean.ALLOCATOR);
        cAsn1Boolean.defineAnnotatedMethods(Asn1Boolean.class);
        cAsn1Integer         = mAsn1.defineClassUnder("Integer", cAsn1Primitive, Asn1Integer.ALLOCATOR);
        cAsn1Integer.defineAnnotatedMethods(Asn1Integer.class);
        cAsn1Enumerated      = mAsn1.defineClassUnder("Enumerated", cAsn1Primitive, Asn1Enumerated.ALLOCATOR);
        cAsn1Enumerated.defineAnnotatedMethods(Asn1Enumerated.class);
        cAsn1BitString       = mAsn1.defineClassUnder("BitString", cAsn1Primitive, Asn1BitString.ALLOCATOR);
        cAsn1BitString.defineAnnotatedMethods(Asn1BitString.class);
        cAsn1OctetString     = mAsn1.defineClassUnder("OctetString",cAsn1Primitive, Asn1OctetString.ALLOCATOR);
        cAsn1OctetString.defineAnnotatedMethods(Asn1OctetString.class);
        cAsn1Utf8String      = mAsn1.defineClassUnder("UTF8String",cAsn1Primitive, Asn1Utf8String.ALLOCATOR);
        cAsn1Utf8String.defineAnnotatedMethods(Asn1Utf8String.class);
        cAsn1NumericString   = mAsn1.defineClassUnder("NumericString",cAsn1Primitive, Asn1NumericString.ALLOCATOR);
        cAsn1NumericString.defineAnnotatedMethods(Asn1NumericString.class);
        cAsn1PrintableString = mAsn1.defineClassUnder("PrintableString",cAsn1Primitive, Asn1PrintableString.ALLOCATOR);
        cAsn1PrintableString.defineAnnotatedMethods(Asn1PrintableString.class);
        cAsn1T61String       = mAsn1.defineClassUnder("T61String",cAsn1Primitive, Asn1T61String.ALLOCATOR);
        cAsn1T61String.defineAnnotatedMethods(Asn1T61String.class);
        cAsn1VideotexString  = mAsn1.defineClassUnder("VideotexString",cAsn1Primitive, Asn1VideotexString.ALLOCATOR);
        cAsn1VideotexString.defineAnnotatedMethods(Asn1VideotexString.class);
        cAsn1Ia5String       = mAsn1.defineClassUnder("IA5String",cAsn1Primitive, Asn1Ia5String.ALLOCATOR);
        cAsn1Ia5String.defineAnnotatedMethods(Asn1Ia5String.class);
        cAsn1GraphicString   = mAsn1.defineClassUnder("GraphicString",cAsn1Primitive, Asn1GraphicString.ALLOCATOR);
        cAsn1GraphicString.defineAnnotatedMethods(Asn1GraphicString.class);
        cAsn1Iso64String     = mAsn1.defineClassUnder("ISO64String",cAsn1Primitive, Asn1Iso64String.ALLOCATOR);
        cAsn1Iso64String.defineAnnotatedMethods(Asn1Iso64String.class);
        cAsn1GeneralString   = mAsn1.defineClassUnder("GeneralString",cAsn1Primitive, Asn1GeneralString.ALLOCATOR);
        cAsn1GeneralString.defineAnnotatedMethods(Asn1GeneralString.class);
        cAsn1UniversalString = mAsn1.defineClassUnder("UniversalString",cAsn1Primitive, Asn1UniversalString.ALLOCATOR);
        cAsn1UniversalString.defineAnnotatedMethods(Asn1UniversalString.class);
        cAsn1BmpString       = mAsn1.defineClassUnder("BMPString",cAsn1Primitive, Asn1BmpString.ALLOCATOR);
        cAsn1BmpString.defineAnnotatedMethods(Asn1BmpString.class);
        cAsn1Null            = mAsn1.defineClassUnder("Null",cAsn1Primitive, Asn1Null.ALLOCATOR);
        cAsn1Null.defineAnnotatedMethods(Asn1Null.class);
        cAsn1ObjectId        = mAsn1.defineClassUnder("ObjectId",cAsn1Primitive, Asn1ObjectId.ALLOCATOR);
        cAsn1ObjectId.defineAnnotatedMethods(Asn1ObjectId.class);
        cAsn1UtcTime         = mAsn1.defineClassUnder("UTCTime",cAsn1Primitive, Asn1UtcTime.ALLOCATOR);
        cAsn1UtcTime.defineAnnotatedMethods(Asn1UtcTime.class);
        cAsn1GeneralizedTime = mAsn1.defineClassUnder("GeneralizedTime",cAsn1Primitive, Asn1GeneralizedTime.ALLOCATOR);
        cAsn1GeneralizedTime.defineAnnotatedMethods(Asn1GeneralizedTime.class);
        cAsn1Sequence        = mAsn1.defineClassUnder("Sequence",cAsn1Constructive, Asn1Sequence.ALLOCATOR);
        cAsn1Sequence.defineAnnotatedMethods(Asn1Sequence.class);
        cAsn1Set             = mAsn1.defineClassUnder("Set",cAsn1Constructive, Asn1Set.ALLOCATOR);
        cAsn1Set.defineAnnotatedMethods(Asn1Set.class);

        cAsn1BitString.attr_accessor(runtime.getCurrentContext(), new IRubyObject[]{runtime.newSymbol("unused_bits")});
        
        try {
            Class<?>[] params = new Class<?>[] { Ruby.class, RubyClass.class, Asn1Object.class };
            ASN1_INFOS = new Object[][] {
                { "END_OF_CONTENTS",   cAsn1EndOfContents  , Asn1EndOfContents.class.getConstructor(params)   },
                { "BOOLEAN",           cAsn1Boolean        , Asn1Boolean.class.getConstructor(params)         },
                { "INTEGER",           cAsn1Integer        , Asn1Integer.class.getConstructor(params)         },
                { "BIT_STRING",        cAsn1BitString      , Asn1BitString.class.getConstructor(params)       },
                { "OCTET_STRING",      cAsn1OctetString    , Asn1OctetString.class.getConstructor(params)     },
                { "NULL",              cAsn1Null           , Asn1Null.class.getConstructor(params)            },
                { "OBJECT_ID",         cAsn1ObjectId       , Asn1ObjectId.class.getConstructor(params)        },
                { "OBJECT_DESCRIPTOR", null                , null                                             },
                { "EXTERNAL",          null                , null                                             },
                { "REAL",              null                , null                                             },
                { "ENUMERATED",        cAsn1Enumerated     , Asn1Enumerated.class.getConstructor(params)      },
                { "EMBEDDED_PDV",      null                , null                                             },
                { "UTF8_STRING",       cAsn1Utf8String     , Asn1Utf8String.class.getConstructor(params)      },
                { "RELATIVE_OID",      null                , null                                             },
                { "[UNIVERSAL 14]",    null                , null                                             },
                { "[UNIVERSAL 15]",    null                , null                                             },
                { "SEQUENCE",          cAsn1Sequence       , Asn1Sequence.class.getConstructor(params)        },
                { "SET",               cAsn1Set            , Asn1Set.class.getConstructor(params)             },
                { "NUMERIC_STRING",    cAsn1NumericString  , Asn1NumericString.class.getConstructor(params)   },
                { "PRINTABLE_STRING",  cAsn1PrintableString, Asn1PrintableString.class.getConstructor(params) },
                { "T61_STRING",        cAsn1T61String      , Asn1T61String.class.getConstructor(params)       },
                { "VIDEOTEX_STRING",   cAsn1VideotexString , Asn1VideotexString.class.getConstructor(params)  },
                { "IA5_STRING",        cAsn1Ia5String      , Asn1Ia5String.class.getConstructor(params)       },
                { "UTC_TIME",          cAsn1UtcTime        , Asn1UtcTime.class.getConstructor(params)         },
                { "GENERALIZED_TIME",  cAsn1GeneralizedTime, Asn1GeneralizedTime.class.getConstructor(params) },
                { "GRAPHIC_STRING",    cAsn1GraphicString  , Asn1GraphicString.class.getConstructor(params)   },
                { "ISO64_STRING",      cAsn1Iso64String    , Asn1Iso64String.class.getConstructor(params)     },
                { "GENERAL_STRING",    cAsn1GeneralString  , Asn1GeneralString.class.getConstructor(params)   },
                { "UNIVERSAL_STRING",  cAsn1UniversalString, Asn1UniversalString.class.getConstructor(params) },
                { "CHARACTER_STRING",  null                , null                                             },
                { "BMP_STRING",        cAsn1BmpString      , Asn1BmpString.class.getConstructor(params)       }
            };
        } catch (Exception ex) {
            throw runtime.newRuntimeError(ex.getMessage());
        }
        
        List<IRubyObject> ary = new ArrayList<IRubyObject>();
        mAsn1.setConstant("UNIVERSAL_TAG_NAME",runtime.newArray(ary));
        for(int i=0; i<ASN1_INFOS.length; i++) {
            if((((String)ASN1_INFOS[i][0])).charAt(0) != '[') {
                ary.add(runtime.newString(((String)(ASN1_INFOS[i][0]))));
                mAsn1.defineConstant(((String)(ASN1_INFOS[i][0])), runtime.newFixnum(i));
            } 
            else {
                ary.add(runtime.getNil());
            }
        }
        
        Parser.createParser(runtime, mAsn1);
        Header.createHeader(runtime, mAsn1);
    }    
}
