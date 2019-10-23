package io.hyscale.servicespec.model;

/**
 * Defines operation to get metadata for field
 * @author tushart
 *
 */
public interface FieldMetaDataProvider {

	FieldMetaData getMetaData(String field);

}
