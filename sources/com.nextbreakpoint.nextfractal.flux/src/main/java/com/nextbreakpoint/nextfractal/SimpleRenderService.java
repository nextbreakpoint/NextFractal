package com.nextbreakpoint.nextfractal;

import java.nio.IntBuffer;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import com.nextbreakpoint.nextfractal.render.RenderFactory;

public class SimpleRenderService implements RenderService {
	private static final Logger logger = Logger.getLogger(SimpleRenderService.class.getName());
	private ExecutorCompletionService<ExportJob> service;
	private ThreadFactory threadFactory;
	private RenderFactory renderFactory;
	
	/**
	 * @param threadFactory
	 * @param renderFactory
	 */
	public SimpleRenderService(ThreadFactory threadFactory, RenderFactory renderFactory) {
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
		service = new ExecutorCompletionService<>(Executors.newFixedThreadPool(5, threadFactory));
	}
	
	@Override
	public Future<ExportJob> dispatch(ExportJob job) {
		return service.submit(new ProcessJobCallable(job));
	}

	private class ProcessJobCallable implements Callable<ExportJob> {
		private ExportJob job;
		
		public ProcessJobCallable(ExportJob job) {
			this.job = job;
		}

		private ImageGenerator createImageGenerator(ExportJob job) {
			final ServiceLoader<? extends FractalFactory> plugins = ServiceLoader.load(FractalFactory.class);
			for (FractalFactory plugin : plugins) {
				try {
					if (job.getPluginId().equals(plugin.getId())) {
						return plugin.createImageGenerator(threadFactory, renderFactory, job.getTile());
					}
				} catch (Exception e) {
				}
			}
			return null;
		}

		@Override
		public ExportJob call() throws Exception {
			try {
				logger.fine(job.toString());
				ImageGenerator generator = createImageGenerator(job);
				IntBuffer pixels = generator.renderImage(job.getProfile().getData());
				job.setResult(new ExportResult(pixels, null));
			} catch (Throwable e) {
				job.setResult(new ExportResult(null, e.getMessage()));
			}
			return job;
		}
	}
}
