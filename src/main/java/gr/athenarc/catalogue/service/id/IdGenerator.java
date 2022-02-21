package gr.athenarc.catalogue.service.id;

public interface IdGenerator<T> {

    T createId();

    T createId(T prefix);

    T createId(T prefix, int length);

}
