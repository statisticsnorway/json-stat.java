package org.jsonstat.v1;

import org.jsonstat.v1.parser.JacksonStatParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public final class Stat {
    private final List<Dataset> datasets = new ArrayList<>();

    public Stat(List<Dataset> datasets) {
        this.datasets.addAll(datasets);
    }

    public List<Dataset> getDatasets() {
        return Collections.unmodifiableList(datasets);
    }

    public Optional<Dataset> getDataset(final String id) {

        return datasets.stream().filter(dataset -> dataset.getId().equals(id)).findFirst();

    }

    public Optional<Dataset> getDataset(final int index) {
        return index < datasets.size() ? Optional.of(getDatasets().get(index)) : Optional.<Dataset>empty();
    }

    public static Stat parse(InputStream stream) throws IOException {
        return new JacksonStatParser().parse(stream);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stat stat = (Stat) o;

        if (!datasets.equals(stat.datasets)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return datasets.hashCode();
    }
}
