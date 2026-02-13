package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.async.AbstractDispatcher;
import org.jboss.seam.async.CronSchedule;
import org.jboss.seam.async.Dispatcher;
import org.jboss.seam.async.Schedule;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions.MethodExpression;
import org.jboss.seam.core.Init.ObserverMethod;
import org.jboss.seam.core.Init.ObserverMethodExpression;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.jus.cnj.pje.auditoria.LogLoadEvent;

/**
 * Support for Seam component-driven events
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.EVENT)
@BypassInterceptors
@Name("org.jboss.seam.core.events")
@Install(precedence=BUILT_IN)
public class Events 
{
   
   private static final LogProvider log = Logging.getLogProvider(Events.class);
   
   /**
    * Add a new listener for a given event type
    * 
    * @param type the event type
    * @param methodBindingExpression a method binding, expressed in EL
    * @param argTypes the argument types of the method binding
    */
   @SuppressWarnings("rawtypes")
   public void addListener(String type, String methodBindingExpression, Class... argTypes)
   {
      MethodExpression methodBinding = Expressions.instance().createMethodExpression(methodBindingExpression, Object.class, argTypes);
      Init.instance().addObserverMethodExpression(type, methodBinding);
   }
   
   /**
    * Raise an event that is to be processed synchronously
    * 
    * @param type the event type
    * @param parameters parameters to be passes to the listener method
    */
   public void raiseEvent(String type, Object... parameters)
   {
      //TODO: find a way to map event parameters to params in an EL-defined listener
      log.trace("Processing event:" + type);
      List<Init.ObserverMethodExpression> list = Init.instance().getObserverMethodExpressions(type);
      if (list!=null)
      {
         for (ObserverMethodExpression listener: list )
         {
            listener.getMethodBinding().invoke(parameters);
         }
      }
      List<Init.ObserverMethod> observers = Init.instance().getObserverMethods(type);
      if (observers!=null)
      {
         for (ObserverMethod observer: observers)
         {
            String name = observer.getComponent().getName();
            Object listener = Component.getInstance( name, observer.isCreate(), false );
            if ( observer.getComponent().hasUnwrapMethod() )
            {
               listener = observer.getComponent().getScope().getContext().get(name);
            }
            
            if (listener!=null)
            {
            	if(parameters != null && parameters.getClass().isArray() && parameters.length == 1 && parameters[0] != null) {
            		if(parameters[0].getClass().isArray()) {
            			observer.getComponent().callComponentMethod(listener, observer.getMethod(), (Object[])parameters[0]);
            		}
            		else {
            			observer.getComponent().callComponentMethod(listener, observer.getMethod(), parameters[0]);
            		}
            	}
            	else {
            		observer.getComponent().callComponentMethod(listener, observer.getMethod(), parameters);
            	}
            }
         }
      }
   }
   
   /**
    * Raise an event that is to be processed asynchronously
    * 
    * @param type the event type
    * @param parameters parameters to be passes to the listener method
    */
   public void raiseAsynchronousEvent(String type, Object... parameters)
   {	
	   if(isLogEvent(type)) {
		   LogEventsMessageSender sender = (LogEventsMessageSender)Component.getInstance(LogEventsMessageSender.class);
		   sender.sendMessage(type, parameters);
	   }
	   else {
		   EventsMessageSender sender = (EventsMessageSender)Component.getInstance(EventsMessageSender.class);
		   sender.sendMessage(type, parameters);
	   }
   }

   /**
    * Raise an event that is to be processed according to a "schedule"
    * 
    * @see TimerSchedule (EJB, quartz or JDK timer service)
    * @see CronSchedule (quartz timer service only)
    * 
    * @param type the event type
    * @param schedule the schedule object, specific to the dispatcher strategy
    * @param parameters parameters to be passes to the listener method
    */
   @SuppressWarnings("unchecked")
   public void raiseTimedEvent(String type, Schedule schedule, Object... parameters)
   {
      getDispatcher().scheduleTimedEvent(type, schedule, parameters);
   }
   
   /**
    * Raise an event that is to be processed after successful completion of 
    * the current transaction
    * 
    * @param type the event type
    * @param parameters parameters to be passes to the listener method
    */
   public void raiseTransactionSuccessEvent(String type, Object... parameters)
   {
      getDispatcher().scheduleTransactionSuccessEvent(type, parameters);
   }
   
   /**
    * Raise an event that is to be processed after the current transaction
    * ends
    * 
    * @param type the event type
    * @param parameters parameters to be passes to the listener method
    */
   public void raiseTransactionCompletionEvent(String type, Object... parameters)
   {
      getDispatcher().scheduleTransactionCompletionEvent(type, parameters);
   }
   
   /**
    * @return the Dispatcher object to use for dispatching asynchronous
    * and timed events
    */
   @SuppressWarnings("rawtypes")
   protected Dispatcher getDispatcher()
   {
      return AbstractDispatcher.instance();
   }
   
   public static boolean exists()
   {
      return Contexts.isEventContextActive() && instance()!=null;
   }

   public static Events instance()
   {
      return (Events) Component.getInstance(Events.class, ScopeType.EVENT);
   }

   private boolean isLogEvent(String type) {
	   String[] types = new String[] {
			   							LogLoadEvent.INSERT_EVENT_NAME, 
			   							LogLoadEvent.UPDATE_EVENT_NAME, 
			   							LogLoadEvent.DELETE_EVENT_NAME, 
			   							LogLoadEvent.SELECT_EVENT_NAME
		   							  };
	   return ArrayUtils.contains(types, type);
   }

}