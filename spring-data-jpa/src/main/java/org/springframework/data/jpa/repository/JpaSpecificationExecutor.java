/*
 * Copyright 2008-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jpa.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.Nullable;

/**
 * Interface to allow execution of {@link Specification}s based on the JPA criteria API.
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Diego Krupitza
 * @author Mark Paluch
 * @author Joshua Chen
 */
public interface JpaSpecificationExecutor<T> {

	/**
	 * Returns a single entity matching the given {@link Specification} or {@link Optional#empty()} if none found.
	 *
	 * @param spec must not be {@literal null}.
	 * @return never {@literal null}.
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if more than one entity found.
	 */
	Optional<T> findOne(Specification<T> spec);

	/**
	 * Returns all entities matching the given {@link Specification}.
	 * <p>
	 * If no {@link Specification} is given all entities matching {@code <T>} will be selected.
	 *
	 * @param spec can be {@literal null}.
	 * @return never {@literal null}.
	 */
	List<T> findAll(@Nullable Specification<T> spec);

	/**
	 * Returns a {@link Page} of entities matching the given {@link Specification}.
	 * <p>
	 * If no {@link Specification} is given all entities matching {@code <T>} will be selected.
	 *
	 * @param spec can be {@literal null}.
	 * @param pageable must not be {@literal null}.
	 * @return never {@literal null}.
	 */
	Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable);

	/**
	 * Returns a {@link Page} of entities matching the given {@link Specification}.
	 * <p>
	 * Supports counting the total number of entities matching the {@link Specification}.
	 * <p>
	 *
	 * @param spec can be {@literal null}, if no {@link Specification} is given all entities matching {@code <T>} will be
	 *          selected.
	 * @param countSpec can be {@literal null}，if no {@link Specification} is given all entities matching {@code <T>} will
	 *          be counted.
	 * @param pageable must not be {@literal null}.
	 * @return never {@literal null}.
	 * @since 3.5
	 */
	Page<T> findAll(@Nullable Specification<T> spec, @Nullable Specification<T> countSpec, Pageable pageable);

	/**
	 * Returns all entities matching the given {@link Specification} and {@link Sort}.
	 * <p>
	 * If no {@link Specification} is given all entities matching {@code <T>} will be selected.
	 *
	 * @param spec can be {@literal null}.
	 * @param sort must not be {@literal null}.
	 * @return never {@literal null}.
	 */
	List<T> findAll(@Nullable Specification<T> spec, Sort sort);

	/**
	 * Returns the number of instances that the given {@link Specification} will return.
	 * <p>
	 * If no {@link Specification} is given all entities matching {@code <T>} will be counted.
	 *
	 * @param spec the {@link Specification} to count instances for, must not be {@literal null}.
	 * @return the number of instances.
	 */
	long count(@Nullable Specification<T> spec);

	/**
	 * Checks whether the data store contains elements that match the given {@link Specification}.
	 *
	 * @param spec the {@link Specification} to use for the existence check, ust not be {@literal null}.
	 * @return {@code true} if the data store contains elements that match the given {@link Specification} otherwise
	 *         {@code false}.
	 */
	boolean exists(Specification<T> spec);

	/**
	 * Deletes by the {@link Specification} and returns the number of rows deleted.
	 * <p>
	 * This method uses {@link jakarta.persistence.criteria.CriteriaDelete Criteria API bulk delete} that maps directly to
	 * database delete operations. The persistence context is not synchronized with the result of the bulk delete.
	 * <p>
	 * Please note that {@link jakarta.persistence.criteria.CriteriaQuery} in,
	 * {@link Specification#toPredicate(Root, CriteriaQuery, CriteriaBuilder)} will be {@literal null} because
	 * {@link jakarta.persistence.criteria.CriteriaBuilder#createCriteriaDelete(Class)} does not implement
	 * {@code CriteriaQuery}.
	 * <p>
	 * If no {@link Specification} is given all entities matching {@code <T>} will be deleted.
	 *
	 * @param spec the {@link Specification} to use for the existence check, can not be {@literal null}.
	 * @return the number of entities deleted.
	 * @since 3.0
	 */
	long delete(@Nullable Specification<T> spec);

	/**
	 * Returns entities matching the given {@link Specification} applying the {@code queryFunction} that defines the query
	 * and its result type.
	 * <p>
	 * The query object used with {@code queryFunction} is only valid inside the {@code findBy(…)} method call. This
	 * requires the query function to return a query result and not the {@link FluentQuery} object itself to ensure the
	 * query is executed inside the {@code findBy(…)} method.
	 *
	 * @param spec must not be null.
	 * @param queryFunction the query function defining projection, sorting, and the result type
	 * @return all entities matching the given specification.
	 * @since 3.0
	 * @throws InvalidDataAccessApiUsageException if the query function returns the {@link FluentQuery} instance.
	 */
	<S extends T, R> R findBy(Specification<T> spec, Function<? super SpecificationFluentQuery<S>, R> queryFunction);

	/**
	 * Extension to {@link FetchableFluentQuery} allowing slice results and pagination with a custom count
	 * {@link Specification}.
	 *
	 * @param <T>
	 * @since 3.5
	 */
	interface SpecificationFluentQuery<T> extends FluentQuery.FetchableFluentQuery<T> {

		@Override
		SpecificationFluentQuery<T> sortBy(Sort sort);

		@Override
		SpecificationFluentQuery<T> limit(int limit);

		@Override
		<R> SpecificationFluentQuery<R> as(Class<R> resultType);

		@Override
		default SpecificationFluentQuery<T> project(String... properties) {
			return this.project(Arrays.asList(properties));
		}

		@Override
		SpecificationFluentQuery<T> project(Collection<String> properties);

		/**
		 * Get a page of matching elements for {@link Pageable} and provide a custom {@link Specification count
		 * specification}.
		 *
		 * @param pageable the pageable to request a paged result, can be {@link Pageable#unpaged()}, must not be
		 *          {@literal null}. The given {@link Pageable} will override any previously specified {@link Sort sort}.
		 *          Any potentially specified {@link #limit(int)} will be overridden by {@link Pageable#getPageSize()}.
		 * @param countSpec specification used to count results.
		 * @return
		 */
		Page<T> page(Pageable pageable, Specification<?> countSpec);
	}

}
