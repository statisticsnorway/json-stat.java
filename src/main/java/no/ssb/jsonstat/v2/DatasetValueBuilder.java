/*
 * The MIT License
 *
 * Copyright 2022 Statistisk sentralbyrå - Statistics Norway
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package no.ssb.jsonstat.v2;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface DatasetValueBuilder {

    /**
     * Populate the data set with values.
     * <p>
     * The values are expected to be flattened in row-major order. See {@link Dataset.ValuesBuilder#withValues(Stream)} for a
     * details about row-major order.
     *
     * @param values the values in row-major order
     * @throws NullPointerException if values is null
     */
    DatasetBuildable withValues(java.util.Collection<Number> values);

    /**
     * Populate the data set with values.
     * <p>
     * The values are expected to be flattened in row-major order. See {@link Dataset.ValuesBuilder#withValues(Stream)} for a
     * details about row-major order.
     *
     * @param values the values in row-major order
     * @throws NullPointerException if values is null
     */
    DatasetBuildable withValues(Iterable<Number> values);

    /**
     * Populate the data set with value lists.
     * <p>
     * The lists are expected to be flattened in row-major order. For example if we have three dimensions
     * (A, B and C) with 3, 2 and 4 categories respectively, the values should be ordered iterating first by the 4
     * categories of C, then by the 2 categories of B and finally by the 3 categories of A:
     * <p>
     * <pre>
     *   A1B1C1   A1B1C2   A1B1C3   A1B1C4
     *   A1B2C1   A1B2C2   A1B2C3   A1B2C4
     *
     *   A2B1C1   A2B1C2   A2B1C3   A1B1C4
     *   A2B2C1   A2B2C2   A2B2C3   A2B2C4
     *
     *   A3B1C1   A3B1C2   A3B1C3   A3B1C4
     *   A3B2C1   A3B2C2   A3B2C3   A3B2C4
     * </pre>
     *
     * @param values the values in row-major order
     * @throws NullPointerException if values is null
     */
    DatasetBuildable withValues(Stream<Number> values);

    /**
     * Use a mapper function to populate the metrics in the data set.
     * <p>
     * The mapper function will be called for every combination of dimensions in
     * row major order (cartesian product of the dimensions).
     * <p>
     * For example if we have three dimensions
     * (A, B and C) with 3, 2 and 4 categories respectively, the function will be called with
     * the parameters ["A1", "B1", "C1"], ["A1", "B1", "C2"], ["A1", "B1", "C4"], ...
     *
     * @param mapper a mapper function to use to populate the metrics in the data set
     * @throws NullPointerException if mapper is null
     */
    DatasetBuildable withMapper(Function<List<String>, Number> mapper);

    /**
     * Add a tuple using the dimension values (categories) and values.
     * <p>
     * Note that this method is permissive in the sense that any category that can
     * not be found in a dimension will be added after the existing ones.
     *
     * @throws IllegalArgumentException if a dimension id is missing, present in both parameters or if an
     *                                  empty map is encountered
     * @throws NullPointerException     is dimensions or metric is null
     */
    DatasetValueBuilder addTuple(List<String> dimensions, Number value);

}
