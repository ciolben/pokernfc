package ch.epfl.pokernfc.Utils;

public interface AsynchHandler<T> {
	void resultReady(T result);
}
