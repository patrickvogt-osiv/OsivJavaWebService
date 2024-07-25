package ch.osiv.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @param <T>
 */
public class JsonBodyHandler<T>
    implements HttpResponse.BodyHandler<Supplier<T>> {

    private final Class<T> targetClass;

    /**
     * @param targetClass
     */
    public JsonBodyHandler(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public HttpResponse.BodySubscriber<Supplier<T>> apply(HttpResponse.ResponseInfo responseInfo) {
        return asJSON(this.targetClass);
    }

    /**
     * @param <W>
     * @param targetType
     * @return Body subscriber
     */
    public static <W> HttpResponse.BodySubscriber<Supplier<W>> asJSON(Class<W> targetType) {
        HttpResponse.BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();

        return HttpResponse.BodySubscribers.mapping(upstream,
                                                    inputStream -> toSupplierOfType(inputStream,
                                                                                    targetType));
    }

    /**
     * @param <W>
     * @param inputStream
     * @param targetType
     * @return Supplier
     */
    public static <W> Supplier<W> toSupplierOfType(InputStream inputStream,
                                                   Class<W> targetType) {
        return () -> {
            try (InputStream stream = inputStream) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(stream, targetType);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
