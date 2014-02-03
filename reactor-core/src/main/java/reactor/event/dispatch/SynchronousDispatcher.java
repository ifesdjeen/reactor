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

package reactor.event.dispatch;

import reactor.event.Event;
import reactor.event.registry.Registry;
import reactor.event.routing.EventRouter;
import reactor.function.Consumer;

import java.util.concurrent.TimeUnit;

/**
 * A {@link Dispatcher} implementation that dispatches events using the calling thread.
 *
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
public final class SynchronousDispatcher implements Dispatcher {

	public SynchronousDispatcher() {
	}

	@Override
	public boolean alive() {
		return true;
	}

	@Override
	public boolean awaitAndShutdown() {
		return true;
	}

	@Override
	public boolean awaitAndShutdown(long timeout, TimeUnit timeUnit) {
		return true;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void halt() {
	}

	@Override
	public <E extends Event<?>> void dispatch(E event,
	                                          EventRouter eventRouter,
	                                          Consumer<E> consumer,
	                                          Consumer<Throwable> errorConsumer) {
		dispatch(null, event, null, errorConsumer, eventRouter, consumer);
	}

	@Override
	public <E extends Event<?>> void dispatch(Object key,
	                                          E event,
	                                          Registry<Consumer<? extends Event<?>>> consumerRegistry,
	                                          Consumer<Throwable> errorConsumer,
	                                          EventRouter eventRouter,
	                                          Consumer<E> completionConsumer) {
		eventRouter.route(key,
		                  event,
		                  (null != consumerRegistry ? consumerRegistry.select(key) : null),
		                  completionConsumer,
		                  errorConsumer);
	}

	@Override
	public void execute(Runnable command) {
		command.run();
	}

}
