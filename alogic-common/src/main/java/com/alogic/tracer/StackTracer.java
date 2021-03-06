package com.alogic.tracer;

import java.util.concurrent.ConcurrentHashMap;

import com.alogic.tracer.log.TraceLog;
import com.anysoft.util.Properties;

/**
 * 调用栈追踪
 * 
 * @author duanyy
 * @since 1.6.5.3
 */
public class StackTracer extends Tracer.Abstract{
	/**
	 * 以线程为单位集合的上下文集合
	 */
	protected ConcurrentHashMap<Long,TraceContext> contexts = new ConcurrentHashMap<Long,TraceContext>();

	@Override
	public TraceContext startProcedure() {
		long thread = Thread.currentThread().getId();
		
		TraceContext current = contexts.get(thread);
		if (current == null){
			//创建一个新的
			current = new TraceContext.Default();
		}else{
			current = current.newChild();
		}
		
		contexts.put(thread, current);
		return current;
	}
	
	@Override
	public TraceContext startProcedure(String sn, long order) {
		long thread = Thread.currentThread().getId();
		
		TraceContext current = contexts.get(thread);
		if (current == null){
			//创建一个新的
			current = new TraceContext.Default(null,sn,order);
		}else{
			current = current.newChild();
		}
		
		contexts.put(thread, current);
		return current;
	}	

	@Override
	public void endProcedure(TraceContext ctx, String type, String name, String result, String note,long contentLength) {
		long thread = Thread.currentThread().getId();
		
		TraceContext current = contexts.get(thread);
		if (current != null){
			TraceContext parent = current.parent();
			if (parent == null){
				contexts.remove(thread);
			}else{
				contexts.put(thread, parent);		
			}
			
			TraceLog traceLog= new TraceLog();
			traceLog.sn(current.sn());
			traceLog.order(current.order());
			traceLog.method(name);
			traceLog.reason(note);
			traceLog.code(result);
			traceLog.startDate(current.timestamp());
			traceLog.duration(System.currentTimeMillis()-current.timestamp());			
			traceLog.contentLength(contentLength);
			
			log(traceLog);			
		}else{
			//如果为空，恐怕出了问题			
			LOG.error("It is impossible,something is wrong.");
		}
	}

	@Override
	public void configure(Properties p) {
		
	}

}
