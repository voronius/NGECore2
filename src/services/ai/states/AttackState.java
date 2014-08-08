/*******************************************************************************
 * Copyright (c) 2013 <Project SWG>
 * 
 * This File is part of NGECore2.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Using NGEngine to work with NGECore2 is making a combined work based on NGEngine. 
 * Therefore all terms and conditions of the GNU Lesser General Public License cover the combination.
 ******************************************************************************/
package services.ai.states;

import java.util.Random;
import java.util.Vector;

import org.python.core.Py;
import org.python.core.PyObject;

import main.NGECore;
import resources.common.FileUtilities;
import resources.objects.creature.CreatureObject;
import resources.objects.weapon.WeaponObject;
import services.ai.AIActor;
import tools.DevLog;

public class AttackState extends AIState {

	
	public class AIEvent
	{
		private boolean done=false;
		public void setDone(boolean done)
		{
			this.done=done;
		}
		public boolean isDone()
		{
			return this.done;
		}
		
	}
	
	public class AttackCmd
	{
		private CreatureObject actor;
		private CreatureObject target;
		private String commandName;
		private String arg1;
		private String arg2;
		private boolean valid=false;
		public AttackCmd()
		{
			this.actor=null;
			this.target=null;
			this.commandName=null;
			this.arg1="";
			this.arg2="";
		}
		public void setActor(CreatureObject actor)
		{
			this.actor=actor;
		}
		public void setTarget(CreatureObject target)
		{
			this.target=target;
		}
		public void setCmd(String cmd)
		{
			this.commandName=cmd;
		}
		public void setArg1(String arg)
		{
			this.arg1=arg;
		}
		
		public void setArg2(String arg)
		{
			this.arg2=arg;
		}
		
		public CreatureObject getActor()
		{
			return this.actor;
		}
		public CreatureObject getTarget()
		{
			return this.target;
		}
		public String getCmd()
		{
			return this.commandName;
		}
		public String getArg1()
		{
			return this.arg1;
		}
		
		public String getArg2()
		{
			return this.arg2;
		}
		
		public void validate()
		{
			this.valid=true;
		}
		
		
		public boolean isValid()
		{
			return this.valid;
		}
		
	}
	

	
	@Override
	public byte onEnter(AIActor actor) {
		DevLog.debugout("Charon", "AI Attack State", "onEnter");
		CreatureObject creature = actor.getCreature();
		if(creature.getPosture() == 14)
			return StateResult.DEAD;
		if(!creature.isInCombat() || creature.getDefendersList().size() == 0 || actor.getFollowObject() == null)
			return StateResult.FINISHED;
		actor.scheduleMovement();
		actor.scheduleRecovery();
		return StateResult.UNFINISHED;
	}

	@Override
	public byte onExit(AIActor actor) {
		// TODO Auto-generated method stub

		actor.getCreature().setLookAtTarget(0);
		actor.getCreature().setIntendedTarget(0);
		actor.setFollowObject(null);
		DevLog.debugout("Charon", "AI Attack State", "onExit");
		return StateResult.FINISHED;
	}

	@Override
	public byte move(AIActor actor) {
		NGECore core = NGECore.getInstance();
		CreatureObject creature = actor.getCreature();
		
		if(creature.getPosture() == 14)
			return StateResult.DEAD;
		actor.getMovementPoints().clear();
		
		if(actor.getFollowObject() != null) 
		{
			if(actor.getSpawnPosition().getWorldPosition().getDistance(creature.getWorldPosition()) > 128 || core.terrainService.isWater(creature.getPlanetId(), actor.getFollowObject().getWorldPosition())) 
			{
				actor.removeDefender(actor.getFollowObject());
				//actor.scheduleMovement();
				return StateResult.UNFINISHED;
			}
			float maxDistance = 0;
			WeaponObject weapon = null;	
			if(creature.getWeaponId() != 0)
			{
				weapon = (WeaponObject) NGECore.getInstance().objectService.getObject(creature.getWeaponId());
				if(weapon != null)
				{
					maxDistance = weapon.getMaxRange() - 1;
				}
			} 
			else if(creature.getSlottedObject("default_weapon") != null)
			{
				weapon = (WeaponObject) creature.getSlottedObject("default_weapon");
				
				if(weapon != null)
				{
					maxDistance = weapon.getMaxRange() - 1;
				}
			}

			try
			{
				if(actor.getFollowObject().getWorldPosition().getDistance(creature.getWorldPosition()) > maxDistance)
				{
					
					
					//go ranged or follow
					if(!actor.isRanged() )
					{
						if( actor.getRangedWeapon()!=null)
						{
						CreatureObject target=actor.getFollowObject();
						AIEvent evt=new AIEvent();
						if(actor.getMobileTemplate().getAIFile()!=null)
						{

						
							if (FileUtilities.doesFileExist("scripts/ai/" + actor.getMobileTemplate().getAIFile() +  ".py"))
							{
								PyObject method = core.scriptService.getMethod("scripts/ai/", actor.getMobileTemplate().getAIFile(), "onRanged");
						
								if (method != null && method.isCallable()) 
								{
			        			 method.__call__(Py.java2py(core), Py.java2py(actor), Py.java2py(target),Py.java2py(evt));
								}
							}
						 
				        
						}
						else if(FileUtilities.doesFileExist("scripts/ai/genericCombatAI.py"))
						{
							PyObject method = core.scriptService.getMethod("scripts/ai/", "genericCombatAI", "onRanged");
						
							if (method != null && method.isCallable()) 
								{
									method.__call__(Py.java2py(core), Py.java2py(actor), Py.java2py(target),Py.java2py(evt));
								}
			        	 
			 			 
						}
					
						//follow by default and if script didn't set the done flag
						if(!evt.isDone())
						{
							actor.setNextPosition(actor.getFollowObject().getPosition());
						}
						else
						{
						//recover(actor);
							//?!
							actor.setNextPosition(actor.getFollowObject().getPosition());
							
							actor.faceObject(actor.getFollowObject());
							actor.scheduleMovement();
							return StateResult.UNFINISHED;
						}
						
						}

					}
					else
					{

						actor.setNextPosition(actor.getFollowObject().getPosition());
					}
					actor.setNextPosition(actor.getFollowObject().getPosition());
				}
				else
				{
						//recover(actor);
						actor.faceObject(actor.getFollowObject());
						actor.scheduleMovement();
						return StateResult.UNFINISHED;
				}
				
			} 
			catch (Exception e)
			{
				DevLog.debugout("Charon", "AI Attack State Exception move method", "actor " + actor);
				DevLog.debugout("Charon", "AI Attack State Exception move method", "actor.getFollowObject() " + actor.getFollowObject());
				DevLog.debugout("Charon", "AI Attack State Exception move method", "actor.getFollowObject().getWorldPosition() " + actor.getFollowObject().getWorldPosition());
				DevLog.debugout("Charon", "AI Attack State Exception move method", "creature.getWorldPosition() " + creature.getWorldPosition());
			}

		}
		else
		{
			return StateResult.FINISHED;
		}
		
		doMove(actor);
		actor.scheduleMovement();
		return StateResult.UNFINISHED;
	}

	@Override
	public byte recover(AIActor actor) {

	
		CreatureObject creature = actor.getCreature();
		CreatureObject target = actor.getFollowObject();
		float maxDistance = 0;
		WeaponObject weapon = null;
		if(creature.getWeaponId() != 0) {
			weapon = (WeaponObject) NGECore.getInstance().objectService.getObject(creature.getWeaponId());
			if(weapon != null)
				maxDistance = weapon.getMaxRange() - 1;
		} else if(creature.getSlottedObject("default_weapon") != null) {
			weapon = (WeaponObject) creature.getSlottedObject("default_weapon");
			if(weapon != null)
				maxDistance = weapon.getMaxRange() - 1;
		}
		if(weapon == null)
		{
		
			return StateResult.FINISHED;
		}
		if(actor.getTimeSinceLastAttack() < weapon.getAttackSpeed() * 1000) 
		{
			
			//actor.scheduleRecovery();
			return StateResult.UNFINISHED;
		}
		
		NGECore core = NGECore.getInstance();
		if(creature.getPosture() == 14)
		{
		
			return StateResult.DEAD;
		}
		if(!creature.isInCombat() || creature.getDefendersList().size() == 0 || actor.getFollowObject() == null)
		{

			
			if(actor.getMobileTemplate().getAIFile()!=null)
			{
				AIEvent evt=new AIEvent();
				
	        	 if (FileUtilities.doesFileExist("scripts/ai/" + actor.getMobileTemplate().getAIFile() +  ".py"))
	        	 {
	        		 PyObject method = core.scriptService.getMethod("scripts/ai/", actor.getMobileTemplate().getAIFile(), "onPeace");
				
	        		 if (method != null && method.isCallable()) 
	        		 {
	        			 method.__call__(Py.java2py(core), Py.java2py(actor), Py.java2py(evt));
	        		 }
	        	 }
				/* if (!evt.isDone()) do some core processing */ 
		        
			}
			else if(FileUtilities.doesFileExist("scripts/ai/genericCombatAI.py"))
			{
					AIEvent evt=new AIEvent();
					 PyObject method = core.scriptService.getMethod("scripts/ai/", "genericCombatAI", "onPeace");
				
	        		 if (method != null && method.isCallable()) 
	        		 {
	        			 method.__call__(Py.java2py(core), Py.java2py(actor), Py.java2py(evt));
	        		 }
	        	 
	 				/* if (!evt.isDone()) do some core processing */ 
			}
			
			if (creature.getLookAtTarget() != 0)
				creature.setLookAtTarget(0);
			if (creature.getIntendedTarget() != 0)
				creature.setIntendedTarget(0);
			actor.setFollowObject(null);
			actor.setCurrentState(new RetreatState());
			return StateResult.FINISHED;
		}
		
		CreatureObject potentialTarget=actor.getHighestAggro();
		if(target != potentialTarget  && potentialTarget != null)
		{
			
			AIEvent evt=new AIEvent();
			if(actor.getMobileTemplate().getAIFile()!=null)
			{

			
				if (FileUtilities.doesFileExist("scripts/ai/" + actor.getMobileTemplate().getAIFile() +  ".py"))
				{
					PyObject method = core.scriptService.getMethod("scripts/ai/", actor.getMobileTemplate().getAIFile(), "onAggroChange");
			
					if (method != null && method.isCallable()) 
					{
        			 method.__call__(Py.java2py(actor), Py.java2py(target),Py.java2py(potentialTarget),Py.java2py(evt));
					}
				}
			 
	        
			}
			else if(FileUtilities.doesFileExist("scripts/ai/genericCombatAI.py"))
			{
				PyObject method = core.scriptService.getMethod("scripts/ai/", "genericCombatAI", "onAggroChange");
			
				if (method != null && method.isCallable()) 
					{
						method.__call__( Py.java2py(actor), Py.java2py(target),Py.java2py(potentialTarget),Py.java2py(evt));
					}
        	 
 			 
			}
		
			//follow by default and if script didn't set the done flag
			if(!evt.isDone())
			{
				actor.setFollowObject(potentialTarget);
				
			}
			else
			{
				//hack to prevent spamming onAggroChange to often
				actor.addAggro(target, actor.getAggro(potentialTarget)/10);
			}
			
			target = actor.getFollowObject();

		}
		
		if(target == null) 
		{
	
			
			DevLog.debugout("Charon", "AI Attack State", "null target"); 
			actor.scheduleRecovery();
			return StateResult.UNFINISHED;
		}
		if(target.getPosture() == 13 || target.getPosture() == 14 || target.isInStealth()) 
		{
 
			actor.setFollowObject(actor.getHighestDamageDealer());			
			target = actor.getFollowObject();
			if(target == null)
			{
				if (creature.getLookAtTarget() != 0)
					creature.setLookAtTarget(0);
				if (creature.getIntendedTarget() != 0)
					creature.setIntendedTarget(0);
				
			}
			actor.setFollowObject(null);
			actor.removeDefender(target);

			actor.setCurrentState(new RetreatState());

			
			return StateResult.FINISHED;
		}
		if(target.getWorldPosition().getDistance(creature.getWorldPosition()) > 128 || target.getPosture() == 13 || target.getPosture() == 14) 
		{
			actor.removeDefender(target);
			actor.scheduleRecovery();

			
			return StateResult.UNFINISHED;
		}
		
		if(target.getWorldPosition().getDistance(creature.getWorldPosition()) > maxDistance) 
		{

			actor.scheduleRecovery();
			return StateResult.UNFINISHED;
		}
		
		float meleeDistance=5;
		if(weapon.isRanged() && target.getWorldPosition().getDistance(creature.getWorldPosition()) <= meleeDistance)
		{
			AIEvent evt=new AIEvent();
			if(actor.getMobileTemplate().getAIFile()!=null)
			{

			
				if (FileUtilities.doesFileExist("scripts/ai/" + actor.getMobileTemplate().getAIFile() +  ".py"))
				{
					PyObject method = core.scriptService.getMethod("scripts/ai/", actor.getMobileTemplate().getAIFile(), "onMelee");
			
					if (method != null && method.isCallable()) 
					{
        			 method.__call__(Py.java2py(core), Py.java2py(actor), Py.java2py(target),Py.java2py(evt));
					}
				}
			 
	        
			}
			else if(FileUtilities.doesFileExist("scripts/ai/genericCombatAI.py"))
			{
				PyObject method = core.scriptService.getMethod("scripts/ai/", "genericCombatAI", "onMelee");
			
				if (method != null && method.isCallable()) 
					{
						method.__call__(Py.java2py(core), Py.java2py(actor), Py.java2py(target),Py.java2py(evt));
					}
        	 
 			 
			}

		}
		//actor.faceObject(target);
		
		Vector<String> attacks = actor.getMobileTemplate().getAttacks();

		// Pet
//		if (creature.getOwnerId()>0)
//			attacks = creature.getSpecialAttacks();
		if (creature.getLookAtTarget() != target.getObjectId())
			creature.setLookAtTarget(target.getObjectId());
		if (creature.getIntendedTarget() != target.getObjectId())
			creature.setIntendedTarget(target.getObjectId());
		AttackCmd output=new AttackCmd();
		if(actor.getMobileTemplate().getAIFile()!=null && FileUtilities.doesFileExist("scripts/ai/"+actor.getMobileTemplate().getAIFile()+".py"))
		{
			PyObject method = core.scriptService.getMethod("scripts/ai/", actor.getMobileTemplate().getAIFile(), "onAttack");
			if (method != null && method.isCallable()) 
			{
				method.__call__(Py.java2py(core), Py.java2py(actor), Py.java2py(target),Py.java2py(output));
			}
			
	        
		}
		else if(FileUtilities.doesFileExist("scripts/genericCombatAI.py"))
		{
			PyObject method = core.scriptService.getMethod("scripts/ai/", "genericCombatAI", "onAttack");
			if (method != null && method.isCallable()) 
			{
				method.__call__(Py.java2py(core), Py.java2py(actor), Py.java2py(target),Py.java2py(output));
			}	
		}
		if(output.isValid())
		{
			core.commandService.callCreatureCommand(output.getActor(),output.getTarget(),core.commandService.getCommandByName(output.getCmd()),0, output.getArg1()+" "+output.getArg2());
		}
		else if(attacks.size()>0)
		{
			Random rand = new Random();
			core.commandService.callCommand(creature, attacks.get(rand.nextInt(attacks.size())), target, "");
			
		}
		else  
		{
			core.commandService.callCommand(creature, actor.getMobileTemplate().getDefaultAttack(), target, "");
		}


		actor.setLastAttackTimestamp(System.currentTimeMillis());
		actor.scheduleRecovery();
		return StateResult.UNFINISHED;
	}

}
