package com.thefirstlineofcode.sand.server.lite.concentrator;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thefirstlineofcode.sand.server.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.server.concentrator.IConcentratorFactory;
import com.thefirstlineofcode.sand.server.things.Thing;
import com.thefirstlineofcode.sand.server.things.IThingManager;

@Component
@Transactional
public class ConcentratorFactory implements IConcentratorFactory, ApplicationContextAware {
	private ApplicationContext applicationContext;
	
	@Autowired
	private IThingManager thingManager;
	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public boolean isConcentrator(String thingId) {
		if (!thingManager.isRegistered(thingId)) {			
			return false;
		}
		
		Thing thing = thingManager.getByThingId(thingId);
		return thingManager.isConcentrator(thing.getModel());
	}

	@Override
	public IConcentrator getConcentrator(String thingId) {		
		if (!isConcentrator(thingId))
			throw new IllegalArgumentException(String.format("Thing[%s] isn't a concentrator.", thingId));
		
		String concentratorThingName = thingManager.getThingNameByThingId(thingId);
		
		return applicationContext.getBean(Concentrator.class, concentratorThingName, sqlSession);
	}

	@Override
	public boolean isLanNode(String nodeThingId) {
		if (!thingManager.isRegistered(nodeThingId)) {			
			return false;
		}
		
		return getConcentrationMapper().selectCountByNode(nodeThingId) != 0;
	}
	
	private ConcentrationMapper getConcentrationMapper() {
		return sqlSession.getMapper(ConcentrationMapper.class);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public String getConcentratorThingNameByNodeThingId(String nodeThingId) {
		D_Concentration concentration = getConcentrationMapper().selectConcentrationByNode(nodeThingId);
		if (concentration == null)
			return null;
		
		return concentration.getConcentratorThingName();
	}

}
