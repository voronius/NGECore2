import sys
from services.spawn import AITemplate
from services.spawn import MobileTemplate
from services.command import CommandService
from services.command import BaseSWGCommand
from services.command import CombatCommand
from  resources.objects.weapon import WeaponObject;

#needed for uniqueSkills
from services.ai import AIActor

#----------------------- Interface for AIActor documentation ----------------
#this represents the current instance of this NPC - this very one mob !!
#useful maybe in events or instances as such
#interface with AIActor as such :
#- for getting the creature , this is essential :
#   creature=actor.getCreature();
#- for access to template skills :
#	templateSkills=actor.getMobileTemplate().getSkills()
#- for working with skills 
#	Map<String,Integer> getUniqueSkills() 
#	void setUniqueSkill(String k, Integer flags) 
#	void removeUniqueSkill(String k) 
#	void clearUniqueSkills()
#
#- for having states and flags
#	Map<String,Integer> getScriptVars() 
#	void setScriptVar(String k, Integer flags) 
#	void removeScriptVar(String k) 
#	void clearScriptVars()
#
#at any point during script below add (enable ) unique skills for that one mob, no other instance will be affected 
#!!!!!!!! - if it won't meet the conditions later - !!!!!!!!!!!!!!! 
#------------------------ done docs for interface with AIActor -----------------


#needed for events processing
from services.ai.states import AttackState
#on methods that get evt param use evt.setDone(1)
#like for example in onRanged method , to avoid AI following, maybe you pull out a ranged weapon too


#test flags against , those are setup in core in BaseTemplate
#	AITemplate.S_HEAL=1;
#	AITemplate.S_BUFF=2;
#	AITemplate.S_DAMAGE=4;
#	AITemplate.S_DEFENSIVE=8;
#	AITemplate.S_AOE=16;
#	AITemplate.S_DEBUFF=32;
#	AITemplate.S_DRAIN=64;
#	AITemplate.S_RANGED=128;
#	AITemplate.S_MELEE=256;
#or with utility methods
#AITemplate.isHeal(mask)
#flag=AITemplate.setHeal(mask) - returns newmask=oldmask|S_HEAL
#
#AITemplate.isAOE(mask) -notice the spelling 
#flag=AITemplate.setAOE(mask) - returns newmask=oldmask|S_AOE
#the rest are similar






#---------- support for live testing of skills ------
#add skill and flags here 
#format for those command_name=>mask
#those should be used for live testing
#better set the skills through interface for mobile template
commands={}
commands['fs_sh_0']=AITemplate.setHeal(0)
commands['of_pistol_bleed']=AITemplate.setRanged(AITemplate.setDebuff(AITemplate.setDamage(0)))
commands['of_del_ae_dm_dot_1']=AITemplate.setRanged(AITemplate.setDebuff(AITemplate.setDamage(0)))






# !!!!!!!!!!! - how the thing works - !!!!!!!!!!!!!!!!!!
#ai works via hooks
#
#---------------------Attack Hook -----------------------------------
# onAttack(core,actor,target,command)
# observe that actor is not the creature !!!
#
#when the ai can attack a hook is enabled on core to return the next attack by calling this method
#
#when no attack is returned :
# 1.there's no AI script for this template in scripts/ai/
# 2.there's no genericCombatAI file in scripts/ai
# or !!!!!!!! very important !!!!!!!!!!!!
# 3. whatever the file called didn't called 	"command.validate()"
# the behavior will be the default one
#
#There are 3 waves of skills maps <String cmd_name,Integer flags>
#examined : 
#1.uniqueSkills - those will mostly be empty 
#2.templateSkills - those hold the skills specific for this NPC template class
#3.this file skills - those in commands , those are usefull for live testing but also , more important, 
#	for bypassing template restriction like defining a set of skills for all ranged npc in scripts/ai/genericRanged.py
#	and then set the template ai file to genericRanged 
#
#At the end of the examination you retain one of each relevant skill if available by examining the flags:
#in this example I have retained a heal and a damage skill and i also gathered all available damage skills in 
#attacks vector
#When you have the possible skills you make your choice based on creature parameters like hp
#--------------------- End AttackHook -------------------------------------------------

#--------------------- onKill Hook -----------------------------------
#onKill(core,actor,killer):
# observe that actor is not the creature !!!
#
#called when the NPC dies (as a result of combat) 
#example future usage  - quests, statistics ..., just remove the method if not used
#--------------------- End onKill Hook -------------------------------------------------


#--------------------- onRanged Hook -----------------------------------
#onRanged(core,actor,target,evt):
# observe that actor is not the creature !!!
#called when the NPC is in combat and target has gone outside melee range but npc has melee weapon
#use evt.setDone(1) to avoid default behaviour of following the target but do set some ranged weapon to avoid odd situations
#remove the method or
#use the method and not set done on event to keep the default behaviour too, like cast a speed buff :)) 
#--------------------- End onRanged Hook -------------------------------------------------


#--------------------- onMelee Hook -----------------------------------
#onMelee(core,actor,target,evt):
# observe that actor is not the creature !!!
#called when the NPC is in combat and target has gone into melee (float range=5 :) )  range but npc has ranged weapon
#do set some melee weapon to avoid odd situations, core has no handling of this that i know of
#remove the method or
#evt is currently ignored cause of lack of handling in core 
#--------------------- End onMelee Hook -------------------------------------------------


#--------------------- onAggroChange Hook -----------------------------------
#onAggroChange(actor,target,newtarget,evt):
# observe that this lacks core access, but i think you can get access by importing it and calling getInstance on it !!!
#called when the NPC is in combat and target is not the highest damage dealer
#do set some melee weapon to avoid odd situations, core has no handling of this that i know of
#remove the method or
#evt is currently ignored cause of lack of handling in core 
#--------------------- End onAggroChange Hook -------------------------------------------------


#--------------------- onPeace Hook -----------------------------------
#here you're given the chance for some cleanup/reset of scriptVars
#evt is currently ignored 
#--------------------- End onAggroChange Hook -------------------------------------------------


def onAttack(core,actor,target,command):
	creature=actor.getCreature();
	uniqueSkills=actor.getUniqueSkills()
	templateSkills=actor.getMobileTemplate().getSkills()	
	hp_ratio=creature.getHealth()/creature.getMaxHealth()
	damage=''
	heal=''
	attacks=[]

	






#handle uniqueSkills first - those are unique to this one mob
#see above how to enable unique skills at some point	
	for k in uniqueSkills:
		v=uniqueSkills[k]
		bc=core.commandService.getCommandByName(k)
		if creature.hasCooldown(bc.getCooldownGroup()) or creature.hasCooldown(bc.getCommandName()) :
			continue
		if	heal=='' and AITemplate.isHeal(v) :
			heal=k
		elif AITemplate.isDamage(v):
		 	if AITemplate.isMelee(v) and actor.isRanged() :
		 		continue
		 	elif AITemplate.isRanged(v) and not actor.isRanged() :
		 		continue	
			if	damage=='' :
				damage=k
			attacks.append(k)
			

	for k in templateSkills :
		v=templateSkills[k]
		bc=core.commandService.getCommandByName(k)
		if creature.hasCooldown(bc.getCooldownGroup()) or creature.hasCooldown(bc.getCommandName()) :
			continue
		if	heal=='' and AITemplate.isHeal(v) :
			heal=k
		elif AITemplate.isDamage(v):
		 	if not AITemplate.isMelee(v) and actor.isRanged() :
				if	damage=='' :
					damage=k
				attacks.append(k)
			elif not AITemplate.isRanged(v) and not actor.isRanged() :
				if	damage=='' :
					damage=k
				attacks.append(k)



	for k in commands :
		v=commands[k]
		bc=core.commandService.getCommandByName(k)
		if creature.hasCooldown(bc.getCooldownGroup()) or creature.hasCooldown(bc.getCommandName()) :
			continue
		if	heal=='' and AITemplate.isHeal(v) :
			heal=k
		elif AITemplate.isDamage(v):
		 	if not AITemplate.isMelee(v) and actor.isRanged() :
				if	damage=='' :
					damage=k
				attacks.append(k)
			elif not AITemplate.isRanged(v) and not actor.isRanged() :
				if	damage=='' :
					damage=k
				attacks.append(k)
			

	if hp_ratio<0.75 and heal!='' :
		command.setActor(creature)
		command.setTarget(creature)
		command.setCmd(heal)
	elif damage != '':
		command.setActor(creature)
		command.setTarget(target)
		command.setCmd(damage)
	elif len(attacks)>0:
		random.seed(creature.getObjectId())
		cmd=attacks[random.randrange(0,attacks.length)]
		command.setActor(creature)
		command.setTarget(target)
		command.setCmd(cmd)
	else:
#this means default old behavior
		return
#this effectively enables this ai choice	
	command.validate()
	
#note to remove this by default to avoid calls to an empty method		
def onKill(core,actor,killer):
	print 'onKill by ',killer.getCustomName()
	return
#note to remove this by default to avoid calls to an empty method	
def onRanged(core,actor,target,evt):
	actor.equipRangedWeapon()
	print 'onRanged vs ',target.getCustomName()
 	return
#note to remove this by default to avoid calls to an empty method	
def onMelee(core,actor,target,evt):
	print 'onMelee vs ',target.getCustomName()
	actor.equipMeleeWeapon()
 	return

#note to remove this by default to avoid calls to an empty method	
def onPeace(core,actor,evt):
 	return


#note to remove this by default to avoid calls to an empty method	
#note, no core access !!!
def onAggroChange(actor,target,newtarget,evt):
	print 'onAggroChange'
 	return	