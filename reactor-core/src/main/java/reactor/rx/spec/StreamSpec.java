/*
 * Copyright (c) 2011-2013 GoPivotal, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.rx.spec;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.Environment;
import reactor.core.Observable;
import reactor.event.Event;
import reactor.event.dispatch.Dispatcher;
import reactor.event.selector.Selector;
import reactor.function.Consumer;
import reactor.function.Supplier;
import reactor.rx.Stream;
import reactor.rx.action.Action;
import reactor.rx.action.ForEachAction;
import reactor.rx.action.SupplierAction;
import reactor.util.Assert;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A helper class for specifying a bounded {@link Stream}. {@link #each} must be called to
 * provide the stream with its values.
 *
 * @param <T> The type of values that the stream contains.
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
public final class StreamSpec<T> extends PipelineSpec<StreamSpec<T>, Stream<T>> {

	private int batchSize = Integer.MAX_VALUE;
	private Iterable<T>  values;
	private Supplier<T>  valuesSupplier;
	private Publisher<T> source;

	/**
	 * Configures the stream to have the given {@code capacity}.
	 *
	 * @param batchSize The batch size of the stream
	 * @return {@code this}
	 */
	public StreamSpec<T> capacity(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	/**
	 * Configures the stream to tap data from the given {@param publisher}.
	 *
	 * @param publisher The {@link Publisher<T>}
	 * @return {@code this}
	 */
	public StreamSpec<T> source(Publisher<T> publisher) {
		this.source = publisher;
		return this;
	}
	/**
	 *
	 * Configures the stream to tap data from the given {@param observable} and {@param selector}.
	 *
	 * @param observable The {@link Observable} to consume events from
	 * @param selector The {@link Selector} to listen to
	 * @return {@code this}
	 */
	public StreamSpec<T> source(final Observable observable, final Selector selector) {
		this.source = publisherFrom(observable, selector);
		return this;
	}

	/**
	 * Configures the stream to contain the given {@code values}.
	 *
	 * @param values The stream's values
	 * @return {@code this}
	 */
	public StreamSpec<T> each(Iterable<T> values) {
		this.values = values;
		if(Collection.class.isAssignableFrom(values.getClass())){
			this.batchSize = ((Collection<T>)values).size();
		}
		return this;
	}


	/**
	 * Configures the stream to pass value from a {@link Supplier} on flush.
	 *
	 * @param supplier The stream's value generator
	 * @return {@code this}
	 */
	public StreamSpec<T> generate(Supplier<T> supplier) {
		this.valuesSupplier = supplier;
		return this;
	}

	@Override
	protected Stream<T> createPipeline(Environment env, Dispatcher dispatcher) {

		Assert.state(dispatcher.supportsOrdering(), "Dispatcher provided doesn't support event ordering. To use " +
				"MultiThreadDispatcher, refer to #parallel() method. ");

		if(valuesSupplier != null){
			return new SupplierAction<Void, T>(dispatcher, valuesSupplier).env(env).capacity(batchSize);
		}else if(values != null){
			return new ForEachAction<T>(values, dispatcher).env(env).capacity(batchSize);
		}else{
			Stream<T> stream;
			if (source != null) {
				stream = new StreamWithDelegatePublisher<T>(dispatcher, source).env(env).capacity(batchSize).share();
			} else{
				stream = new Stream<T>(dispatcher, env, batchSize).capacity(batchSize);
			}
			return stream;
		}
	}

	static <T> Publisher<T> publisherFrom(final Observable observable, final Selector selector) {
		return new Publisher<T>() {
			@Override
			public void subscribe(final Subscriber<? super T> subscriber) {
				observable.on(selector, new Consumer<Event<T>>() {
					@Override
					public void accept(Event<T> event) {
						subscriber.onNext(event.getData());
					}
				});
			}
		};
	}


	static class StreamWithDelegatePublisher<T> extends Stream<T>{
		private final Publisher<? extends T>           publisher;

		public StreamWithDelegatePublisher(@Nonnull Dispatcher dispatcher, Publisher<? extends T> publisher) {
			super(dispatcher);
			this.publisher = publisher;
		}

		@Override
		@SuppressWarnings("unchecked")
		public StreamWithDelegatePublisher<T> env(Environment environment) {
			return (StreamWithDelegatePublisher<T>) super.env(environment);
		}

		@Override
		@SuppressWarnings("unchecked")
		public StreamWithDelegatePublisher<T> capacity(long elements) {
			return (StreamWithDelegatePublisher<T>) super.capacity(elements);
		}

		public Action<T, T> share(){

			return new Action<T, T>() {
				@Override
				protected void doNext(T ev) {
					broadcastNext(ev);
				}

				@Override
				public void subscribe(final Subscriber<? super T> subscriber) {
					final Action<T, T> thiz = this;

					dispatch(new Consumer<Void>() {
						@Override
						public void accept(Void aVoid) {
							thiz.subscribe(subscriber);
							publisher.subscribe(thiz);
						}
					});
				}
			};
		}

		@Override
		public void subscribe(Subscriber<? super T> subscriber) {
			publisher.subscribe(subscriber);
		}
	}
}