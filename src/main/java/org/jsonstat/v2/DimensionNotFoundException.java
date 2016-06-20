package org.jsonstat.v2;

/**
 * Created by hadrien on 15/06/16.
 */
public class DimensionNotFoundException extends RuntimeException {

    private final String dimensionName;
    private final Dataset dataset;

    public DimensionNotFoundException(String message, String dimensionName, Dataset dataset) {
        super(message);
        this.dimensionName = dimensionName;
        this.dataset = dataset;
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public Dataset getDataset() {
        return dataset;
    }
}
