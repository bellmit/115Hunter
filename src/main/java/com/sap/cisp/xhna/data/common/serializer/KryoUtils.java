package com.sap.cisp.xhna.data.common.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.CachedField;
import com.sap.cisp.xhna.data.common.index.PostIdsHolder;

public class KryoUtils {

    // ------------------------------------------------------------
    // Serializers

    /** This is the most basic Kryo usage. Don't register anything go. */
    public static class DefaultSerializer<T> extends Serializer<T> {
        final com.esotericsoftware.kryo.Kryo kryo;

        private final String name;
        private final byte[] buffer = new byte[OUTPUT_BUFFER_SIZE];
        private final Output output = new Output(buffer, -1);
        private final Input input = new Input(buffer);
        private final Class<T> type;
 
        public DefaultSerializer(TypeHandler<T> handler, boolean shared,
                String name) {
            this.name = name;
            this.type = handler.type;
            this.kryo = new com.esotericsoftware.kryo.Kryo();
            kryo.setReferences(shared);
            kryo.setRegistrationRequired(false);
        }

        public T deserialize(byte[] array) {
            input.setBuffer(array);
            return (T) kryo.readObject(input, type);
        }

        public byte[] serialize(T content) {
            output.setBuffer(buffer, -1);
            kryo.writeObject(output, content);
            return output.toBytes();
        }

        public void serializeItems(T[] items, OutputStream outStream)
                throws Exception {
            output.setOutputStream(outStream);
            for (int i = 0, n = items.length; i < n; ++i) {
                kryo.writeClassAndObject(output, items[i]);
            }
            output.flush();
        }

        @SuppressWarnings("unchecked")
        public T[] deserializeItems(InputStream inStream, int numberOfItems)
                throws IOException {
            input.setInputStream(inStream);
            String[] result = new String[numberOfItems];
            for (int i = 0; i < numberOfItems; ++i) {
                result[i] = (String) kryo.readClassAndObject(input);
            }
            return (T[]) result;
        }

        public final String getName() {
            return name;
        }
    }

    /** This is slightly advanced Kryo usage. Just register the classes and go. */
    public static class BasicSerializer<T> extends Serializer<T> {
        private final Class<T> type;
        final com.esotericsoftware.kryo.Kryo kryo;

        private final String name;
        private final byte[] buffer = new byte[OUTPUT_BUFFER_SIZE];
        private final Output output = new Output(buffer, -1);
        private final Input input = new Input(buffer);
        protected boolean isCompressed = true;

        public BasicSerializer(TypeHandler<T> handler, String name) {
            this.type = handler.type;
            this.kryo = new com.esotericsoftware.kryo.Kryo();
            kryo.setReferences(false);
            kryo.setRegistrationRequired(true);
            this.name = name;
            handler.register(this.kryo);
        }

        public T deserialize(byte[] array) {
            synchronized (buffer) {
                if (isCompressed) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(array);
                    InputStream in = (InputStream) bis;
                    in = new InflaterInputStream(in);
                    Input input = new Input(in);
                    T obj = null;
                    //EOF method is not available for ZIB
                    obj = kryo.readObject(input, type);
                    return obj;
                } else {
                    input.setBuffer(array);
                    T obj = null;
                    while (!input.eof()) {
                        obj = kryo.readObject(input, type);
                    }
                    return obj;
                }
            }
        }

        public byte[] serialize(T content) {
            synchronized (buffer) {
                if (isCompressed) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                            16384);
                    DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(
                            byteArrayOutputStream);
                    Output output = new Output(deflaterOutputStream);
                    kryo.writeObject(output, content);
                    output.close();
                    return byteArrayOutputStream.toByteArray();
                } else {
                    output.setBuffer(buffer, -1);
                    output.clear();
                    kryo.writeObject(output, content);
                    return output.toBytes();
                }
            }
        }

        public void serializeItems(T[] items, OutputStream outStream)
                throws Exception {
            output.setOutputStream(outStream);
            for (int i = 0, n = items.length; i < n; ++i) {
                kryo.writeObject(output, items[i]);
            }
            output.flush();
        }

        @SuppressWarnings("unchecked")
        public T[] deserializeItems(InputStream inStream, int numberOfItems)
                throws IOException {
            input.setInputStream(inStream);
            String[] result = new String[numberOfItems];
            for (int i = 0; i < numberOfItems; ++i) {
                result[i] = kryo.readObject(input, String.class);
            }
            return (T[]) result;
        }

        public final String getName() {
            return name;
        }
    }

    /**
     * This shows how to configure individual Kryo serializersto reduce the
     * serialized bytes.
     */
    public static class OptimizedSerializer<T> extends BasicSerializer<T> {
        public OptimizedSerializer(TypeHandler<T> handler) {
            super(handler, "kryo-opt");
            handler.optimize(this.kryo);
            isCompressed = true;

        }
    }

    /**
     * This shows how to use hand written serialization code with Kryo, while
     * still leveraging Kryo for most of the work. A serializer for each class
     * can be implemented, as it is here, or the classes to be serialized can
     * implement an interface and host their own serialization code (similar to
     * java.io.Externalizable).
     */
    public static class CustomSerializer<T> extends BasicSerializer<T> {
        public CustomSerializer(TypeHandler<T> handler) {
            super(handler, "kryo-manual");
            handler.registerCustom(this.kryo);
            isCompressed = true;
        }
    }

    // ------------------------------------------------------------

    public static abstract class TypeHandler<T> {
        public final Class<T> type;

        protected TypeHandler(Class<T> type) {
            this.type = type;
        }

        public abstract void register(com.esotericsoftware.kryo.Kryo kryo);

        public abstract void optimize(com.esotericsoftware.kryo.Kryo kryo);

        public abstract void registerCustom(com.esotericsoftware.kryo.Kryo kryo);
    }

    // ------------------------------------------------------------
    // Post ID list

    public static final TypeHandler<PostIdsHolder> PostIdsContainerTypeHandler = new TypeHandler<PostIdsHolder>(
            PostIdsHolder.class) {
        public void register(com.esotericsoftware.kryo.Kryo kryo) {
            kryo.register(ArrayList.class);
            kryo.register(String.class);
            kryo.register(PostIdsHolder.class);
            kryo.register(HashMap.class);
        }

        @SuppressWarnings("rawtypes")
        public void optimize(com.esotericsoftware.kryo.Kryo kryo) {
            FieldSerializer postIdsContainerSerializer = (FieldSerializer) kryo
                    .getSerializer(PostIdsHolder.class);
            postIdsContainerSerializer.setFieldsCanBeNull(false);

            CachedField idsListField = postIdsContainerSerializer
                    .getField("idList");
            CollectionSerializer idsSerializer = new CollectionSerializer();
            idsSerializer.setElementsCanBeNull(false);
            idsListField.setClass(ArrayList.class, idsSerializer);
        }

        public void registerCustom(com.esotericsoftware.kryo.Kryo kryo) {
            kryo.register(PostIdsHolder.class,
                    new PostIdsContainerSerializer(kryo));
        }
    };

    static class PostIdsContainerSerializer extends
            com.esotericsoftware.kryo.Serializer<PostIdsHolder> {
        private CollectionSerializer idListSerializer;

        public PostIdsContainerSerializer(com.esotericsoftware.kryo.Kryo kryo) {
            idListSerializer = new CollectionSerializer();
            idListSerializer.setElementsCanBeNull(false);
            idListSerializer.setElementClass(String.class,
                    kryo.getSerializer(String.class));
        }

        @SuppressWarnings("unchecked")
        public PostIdsHolder read(com.esotericsoftware.kryo.Kryo kryo,
                Input input, Class<PostIdsHolder> type) {
            final String path = input.readString();
            final List<String> idList = (List<String>) kryo.readObject(input,
                    ArrayList.class, idListSerializer);
            return new PostIdsHolder(path, idList);
        }

        public void write(com.esotericsoftware.kryo.Kryo kryo, Output output,
                PostIdsHolder obj) {
            output.writeString(obj.getPath());
            kryo.writeObject(output, obj.getIdList(), idListSerializer);
        }
    }
}
