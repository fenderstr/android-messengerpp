package org.solovyev.android.messenger.realms.xmpp;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import junit.framework.Assert;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.realms.AccountState;
import org.solovyev.android.messenger.realms.RealmConnectionException;
import org.solovyev.android.messenger.realms.RealmRuntimeException;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 8:28 PM
 */
public class XmppAccountUserServiceTest extends AbstractMessengerTestCase {

	@Nonnull
	private XmppRealm realm1;

	@Nonnull
	private XmppRealm realm2;

	@Inject
	@Nonnull
	private XmppRealmDef xmppRealmDef;

	@Nonnull
	private AccountUserService accountUserService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		realm1 = newRealm1();
		realm2 = newRealm2();
		accountUserService = new XmppAccountUserService(realm1, TemporaryXmppConnectionAware.newInstance(realm1));
	}

	public void testGetUserById() throws Exception {
		final RealmConnection realmConnection2 = realm2.newRealmConnection(getInstrumentation().getContext());

		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						realmConnection2.start();
					} catch (RealmConnectionException e) {
						throw new RealmRuntimeException(realm2.getId(), e);
					}
				}
			}).start();

			// wait until realm2 will be connected
			Thread.sleep(100);

			final User user2 = accountUserService.getUserById(realm2.getUser().getEntity().getRealmEntityId());
			Assert.assertNotNull(user2);
			Assert.assertEquals(user2.getEntity().getRealmEntityId(), TestXmppConfiguration.USER_LOGIN2);
			Assert.assertEquals(user2.getEntity().getRealmId(), realm1.getId());
			Assert.assertEquals("Sergey II Solovyev", user2.getPropertyValueByName(User.PROPERTY_FIRST_NAME));
			Assert.assertNull(user2.getPropertyValueByName(User.PROPERTY_LAST_NAME));

			// load self
			final User user1 = accountUserService.getUserById(realm1.getUser().getEntity().getRealmEntityId());
			Assert.assertNotNull(user1);
			Assert.assertEquals(user1.getEntity().getRealmEntityId(), TestXmppConfiguration.USER_LOGIN);
			Assert.assertEquals(user1.getEntity().getRealmId(), realm1.getId());
			Assert.assertEquals("Sergey I Solovyev", user1.getPropertyValueByName(User.PROPERTY_FIRST_NAME));
			Assert.assertNull(user1.getPropertyValueByName(User.PROPERTY_LAST_NAME));

			final User serso = accountUserService.getUserById("se.solovyev@gmail.com");
			Assert.assertNotNull(serso);
			Assert.assertEquals(serso.getEntity().getRealmEntityId(), "se.solovyev@gmail.com");
			Assert.assertEquals(serso.getEntity().getRealmId(), realm1.getId());
			Assert.assertEquals("Sergey", serso.getPropertyValueByName(User.PROPERTY_FIRST_NAME));
			Assert.assertEquals("Solovyev", serso.getPropertyValueByName(User.PROPERTY_LAST_NAME));


		} finally {
			realmConnection2.stop();
		}
	}

	public void testGetUserContacts() throws Exception {
		List<User> contacts1 = accountUserService.getUserContacts(TestXmppConfiguration.USER_LOGIN);
		Assert.assertTrue(contacts1.size() >= 2);
		Assert.assertTrue(Iterables.any(contacts1, new Predicate<User>() {
			@Override
			public boolean apply(@Nullable User contact) {
				return contact != null && contact.getEntity().getRealmEntityId().equals(TestXmppConfiguration.USER_LOGIN2);
			}
		}));

		contacts1 = accountUserService.getUserContacts(TestXmppConfiguration.USER_LOGIN2);
		Assert.assertTrue(contacts1.isEmpty());

	}

	public void testCheckOnlineUsers() throws Exception {
		final RealmConnection realmConnection2 = realm2.newRealmConnection(getInstrumentation().getContext());

		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						realmConnection2.start();
					} catch (RealmConnectionException e) {
						throw new RealmRuntimeException(realm2.getId(), e);
					}
				}
			}).start();

			// wait until realm2 will be connected
			Thread.sleep(100);


			final User user2InRealm1 = Users.newEmptyUser(realm1.newUserEntity(realm2.getUser().getEntity().getRealmEntityId()));
			final List<User> users1 = accountUserService.checkOnlineUsers(Arrays.asList(user2InRealm1, realm1.getUser()));
			Assert.assertNotNull(users1);
			Assert.assertTrue(!users1.isEmpty());

			// todo serso: currently doesn't work => user presence is not changed fast enough to be registered by roster
/*            Assert.assertTrue(Iterables.any(users1, new Predicate<User>() {
				@Override
                public boolean apply(@Nullable User contact) {
                    return contact != null && contact.getRealmUser().getRealmEntityId().equals(TestXmppConfiguration.USER_LOGIN2) && contact.isOnline();
                }
            }));*/

			Assert.assertTrue(Iterables.any(users1, new Predicate<User>() {
				@Override
				public boolean apply(@Nullable User contact) {
					return contact != null && contact.getEntity().getRealmEntityId().equals(TestXmppConfiguration.USER_LOGIN) && contact.isOnline();
				}
			}));

		} finally {
			realmConnection2.stop();
		}
	}

	public void testGetUserProperties() throws Exception {

	}


	@Nonnull
	protected XmppRealm newRealm1() {
		final String realmId = xmppRealmDef.getId() + "~01";
		XmppAccountConfiguration instance = TestXmppConfiguration.getInstance();
		return new XmppRealm(realmId, xmppRealmDef, Users.newEmptyUser(EntityImpl.newInstance(realmId, instance.getLogin())), instance, AccountState.enabled);
	}

	@Nonnull
	protected XmppRealm newRealm2() {
		final String realmId = xmppRealmDef.getId() + "~02";
		XmppAccountConfiguration instance2 = TestXmppConfiguration.getInstance2();
		return new XmppRealm(realmId, xmppRealmDef, Users.newEmptyUser(EntityImpl.newInstance(realmId, instance2.getLogin())), instance2, AccountState.enabled);
	}
}
