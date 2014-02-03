package reactor.event.dispatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.event.Event;
import reactor.event.registry.Registry;
import reactor.event.routing.EventRouter;
import reactor.function.Consumer;
import reactor.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link reactor.event.dispatch.Dispatcher} that traces activity through it.
 *
 * @author Jon Brisbin
 */
public class TraceableDelegatingDispatcher implements Dispatcher {

	private final Dispatcher delegate;
	private final Logger     log;

	public TraceableDelegatingDispatcher(Dispatcher delegate) {
		Assert.notNull(delegate, "Delegate Dispatcher cannot be null.");
		this.delegate = delegate;
		this.log = LoggerFactory.getLogger(delegate.getClass());
	}

	@Override
	public boolean alive() {
		return delegate.alive();
	}

	@Override
	public boolean awaitAndShutdown() {
		if(log.isTraceEnabled()) {
			log.trace("awaitAndShutdown()");
		}
		return delegate.awaitAndShutdown();
	}

	@Override
	public boolean awaitAndShutdown(long timeout, TimeUnit timeUnit) {
		if(log.isTraceEnabled()) {
			log.trace("awaitAndShutdown({}, {})", timeout, timeUnit);
		}
		return delegate.awaitAndShutdown(timeout, timeUnit);
	}

	@Override
	public void shutdown() {
		if(log.isTraceEnabled()) {
			log.trace("shutdown()");
		}
		delegate.shutdown();
	}

	@Override
	public void halt() {
		if(log.isTraceEnabled()) {
			log.trace("halt()");
		}
		delegate.halt();
	}

	@Override
	public <E extends Event<?>> void dispatch(Object key,
	                                          E event,
	                                          Registry<Consumer<? extends Event<?>>> consumerRegistry,
	                                          Consumer<Throwable> errorConsumer,
	                                          EventRouter eventRouter,
	                                          Consumer<E> completionConsumer) {
		if(log.isTraceEnabled()) {
			log.trace("dispatch({}, {}, {}, {}, {}, {})",
			          key,
			          event,
			          consumerRegistry,
			          errorConsumer,
			          eventRouter,
			          completionConsumer);
		}
		delegate.dispatch(key, event, consumerRegistry, errorConsumer, eventRouter, completionConsumer);
	}

	@Override
	public <E extends Event<?>> void dispatch(E event,
	                                          EventRouter eventRouter,
	                                          Consumer<E> consumer,
	                                          Consumer<Throwable> errorConsumer) {
		if(log.isTraceEnabled()) {
			log.trace("dispatch({}, {}, {}, {})", event, eventRouter, consumer, errorConsumer);
		}
		delegate.dispatch(event, eventRouter, consumer, errorConsumer);
	}

	@Override
	public void execute(Runnable command) {
		delegate.execute(command);
	}

}
