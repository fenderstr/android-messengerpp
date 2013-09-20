package org.solovyev.android.messenger.realms;

import com.google.inject.Inject;
import junit.framework.Assert;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.collections.Collections;

import java.util.Collection;

public class SqliteRealmDaoTest extends AbstractMessengerTestCase {

	@Inject
	private RealmDao realmDao;

	@Inject
	private TestRealmDef testRealmDef;

	public void setUp() throws Exception {
		super.setUp();
		realmDao.deleteAllRealms();
	}

	public void testRealmOperations() throws Exception {
		Collection<Realm> realms = realmDao.loadRealms();
		Assert.assertTrue(realms.isEmpty());

		TestAccountConfiguration expectedConfig1 = new TestAccountConfiguration("test_config_field", 42);
		final Realm expected1 = testRealmDef.newRealm("test~01", Users.newEmptyUser(EntityImpl.newInstance("test~01", "user01")), expectedConfig1, AccountState.enabled);
		realmDao.insertRealm(expected1);

		realms = realmDao.loadRealms();
		Assert.assertTrue(realms.size() == 1);
		Realm<TestAccountConfiguration> actual1 = Collections.getFirstCollectionElement(realms);
		Assert.assertNotNull(actual1);
		Assert.assertTrue(expected1.same(actual1));
		Assert.assertTrue(actual1.getConfiguration().equals(expectedConfig1));
		Assert.assertEquals("test_config_field", actual1.getConfiguration().getTestStringField());
		Assert.assertEquals(42, actual1.getConfiguration().getTestIntField());

		realmDao.deleteRealm(expected1.getId());

		realms = realmDao.loadRealms();
		Assert.assertTrue(realms.isEmpty());
	}

	public void testConcreteRealms() throws Exception {
		int index = 0;
		for (RealmDef realmDef : getRealmService().getRealmDefs()) {
			final AccountConfiguration accountConfiguration = (AccountConfiguration) realmDef.getConfigurationClass().newInstance();
			final String realmId = EntityImpl.getRealmId(realmDef.getId(), index);
			Realm expected = realmDef.newRealm(realmId, Users.newEmptyUser(EntityImpl.newInstance(realmId, String.valueOf(index))), accountConfiguration, AccountState.enabled);
			realmDao.insertRealm(expected);
		}

		Collection<Realm> realms = realmDao.loadRealms();
		Assert.assertTrue(realms.size() == 3);
	}

	public void tearDown() throws Exception {
		realmDao.deleteAllRealms();
		super.tearDown();
	}
}
