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
package services.spawn;

import java.util.Map;
import java.util.Vector;


public class AITemplate {
	//those must be powers of 2
	public final static int S_HEAL=1;
	public final static int S_BUFF=2;
	public final static int S_DAMAGE=4;
	public final static int S_DEFENSIVE=8;
	public final static int S_AOE=16;
	public final static int S_DEBUFF=32;
	public final static int S_DRAIN=64;
	public final static int S_RANGED=128;
	public final static int S_MELEE=256;
	
	
	
	
	
	private Vector<WeaponTemplate> meleeWeaponTemplateVector = new Vector<WeaponTemplate>();
	private Vector<WeaponTemplate> rangedWeaponTemplateVector = new Vector<WeaponTemplate>();
	
	private boolean isMelee=true;
	
	
	
	private Map<String,Integer> skills;
	
	/* this holds what comes after "scripts/ai/" without the extension ".py" and
	 * this should not hold "generic" 
	 */
	private String aiFile;
	
	public AITemplate()
	{
		aiFile=null;
	}
	
	public String getAIFile()
	{
		return aiFile;
	}
	public void setAIFile(String file)
	{
		this.aiFile=file;
	}

	public Map<String,Integer> getSkills() {
		return skills;
	}

	public void setSkills(Map<String,Integer> skills) {
		this.skills = skills;
	}

	//utility methods ------
	
	public static boolean isHeal(Integer flag)
	{
		return ((flag&S_HEAL)==S_HEAL);
	}

	public static boolean isBuff(Integer flag)
	{
		return ((flag&S_BUFF)==S_BUFF);
	}
	public static boolean isDamage(Integer flag)
	{
		return ((flag&S_DAMAGE)==S_DAMAGE);
	}

	public static boolean isDefensive(Integer flag)
	{
		return ((flag&S_DEFENSIVE)==S_DEFENSIVE);
	}
	public static boolean isAOE(Integer flag)
	{
		return ((flag&S_AOE)==S_AOE);
	}

	public static boolean isDebuff(Integer flag)
	{
		return ((flag&S_DEBUFF)==S_DEBUFF);
	}
	
	public static boolean isDrain(Integer flag)
	{
		return ((flag&S_DRAIN)==S_DRAIN);
	}
	
	public static boolean isRanged(Integer flag)
	{
		return ((flag&S_RANGED)==S_RANGED);
	}
	
	
	public static boolean isMelee(Integer flag)
	{
		return ((flag&S_MELEE)==S_MELEE);
	}
	
	
	public static Integer setHeal(Integer flag)
	{
		return (flag|S_HEAL);
	}

	public static Integer setBuff(Integer flag)
	{
		return ((flag|S_BUFF));
	}
	public static Integer setDamage(Integer flag)
	{
		return ((flag|S_DAMAGE));
	}

	public static Integer setDefensive(Integer flag)
	{
		return ((flag|S_DEFENSIVE));
	}
	public static Integer setAOE(Integer flag)
	{
		return ((flag|S_AOE));
	}

	public static Integer setDebuff(Integer flag)
	{
		return ((flag|S_DEBUFF));
	}
	
	public static Integer setDrain(Integer flag)
	{
		return ((flag|S_DRAIN));
	}
	
	public static Integer setRanged(Integer flag)
	{
		return ((flag|S_RANGED));
	}
	
	public static Integer setMelee(Integer flag)
	{
		return ((flag|S_MELEE));
	}
	
	public Vector<WeaponTemplate> getMeleeWeaponTemplateVector() 
	{
		return meleeWeaponTemplateVector;
	}

	public void setMeleeWeaponTemplateVector(Vector<WeaponTemplate> weaponTemplateVector) 
	{
		this.meleeWeaponTemplateVector = weaponTemplateVector;
	}

	
	public Vector<WeaponTemplate> getRangedWeaponTemplateVector() 
	{
		return rangedWeaponTemplateVector;
	}

	public void setRangedWeaponTemplateVector(Vector<WeaponTemplate> weaponTemplateVector) 
	{
		this.rangedWeaponTemplateVector = weaponTemplateVector;
	}
	
	public void setMeleeAI(boolean flag)
	{
			this.isMelee=flag;
	}
	public boolean hasMeleeAI()
	{
			return this.isMelee;
	}
}
