package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.EditButtons;

import static org.solovyev.android.messenger.accounts.AccountUiEventType.FinishedState.back;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_edit_finished;
import static org.solovyev.android.messenger.realms.RealmUiEventType.realm_edit_finished;

public class AccountEditButtons<A extends Account<?>> extends EditButtons<BaseAccountConfigurationFragment<A>> {

	public AccountEditButtons(@Nonnull BaseAccountConfigurationFragment<A> fragment) {
		super(fragment);
	}

	protected final void onSaveButtonPressed() {
		final BaseAccountConfigurationFragment<A> fragment = getFragment();
		final AccountConfiguration configuration = fragment.validateData();
		if (configuration != null) {
			final AccountBuilder accountBuilder = fragment.getRealm().newAccountBuilder(configuration, fragment.getEditedAccount());
			fragment.saveAccount(accountBuilder);
		}
	}

	protected void onBackButtonPressed() {
		final BaseAccountConfigurationFragment<A> fragment = getFragment();
		A editedRealm = fragment.getEditedAccount();
		if (editedRealm != null) {
			fragment.getEventManager().fire(account_edit_finished.newEvent(editedRealm, back));
		} else {
			fragment.getEventManager().fire(realm_edit_finished.newEvent(fragment.getRealm()));
		}
	}

	@Override
	protected boolean isRemoveButtonVisible() {
		return !getFragment().isNewAccount();
	}

	@Override
	protected void onRemoveButtonPressed() {
		final BaseAccountConfigurationFragment<A> fragment = getFragment();
		fragment.removeAccount(fragment.getEditedAccount());
	}

	@Override
	protected boolean isBackButtonVisible() {
		final BaseAccountConfigurationFragment<A> fragment = getFragment();
		// in multi pane layout we don't want to show 'Back' button as there is no 'Back' (in one pane we reuse pane for showing more than one fragment and back means to return to the previous fragment)
		return !(fragment.isNewAccount() && fragment.getMultiPaneManager().isDualPane(getActivity()));
	}
}
