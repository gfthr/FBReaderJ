/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.android.application;

import java.util.*;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;

import org.geometerplus.android.util.UIUtil;

public final class ZLAndroidApplicationWindow extends ZLApplicationWindow {
	private final HashMap<MenuItem,String> myMenuItemMap = new HashMap<MenuItem,String>();

	private final MenuItem.OnMenuItemClickListener myMenuListener =
		new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				getApplication().doAction(myMenuItemMap.get(item));
				return true;
			}
		};

	public ZLAndroidApplicationWindow(ZLApplication application) {
		super(application);
	}

	public Menu addSubMenu(Menu menu, String id) {
		return menu.addSubMenu(ZLResource.resource("menu").getResource(id).getValue());
	}

	public void addMenuItem(Menu menu, String actionId, Integer iconId, String name) {
		if (name == null) {
			name = ZLResource.resource("menu").getResource(actionId).getValue();
		}
		final MenuItem menuItem = menu.add(name);
		if (iconId != null) {
			menuItem.setIcon(iconId);
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		menuItem.setOnMenuItemClickListener(myMenuListener);
		myMenuItemMap.put(menuItem, actionId);
	}

	@Override
	public void refresh() {
		for (Map.Entry<MenuItem,String> entry : myMenuItemMap.entrySet()) {
			final String actionId = entry.getValue();
			final ZLApplication application = getApplication();
			final MenuItem menuItem = entry.getKey();
			menuItem.setVisible(application.isActionVisible(actionId) && application.isActionEnabled(actionId));
			switch (application.isActionChecked(actionId)) {
				case B3_TRUE:
					menuItem.setCheckable(true);
					menuItem.setChecked(true);
					break;
				case B3_FALSE:
					menuItem.setCheckable(true);
					menuItem.setChecked(false);
					break;
				case B3_UNDEFINED:
					menuItem.setCheckable(false);
					break;
			}
		}
		final ZLAndroidActivity activity = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getActivity();
		activity.refresh();
	}
	
	@Override
	public void runWithMessage(String key, Runnable action, Runnable postAction) {
		final Activity activity = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getActivity();
		if (activity != null) {
			UIUtil.runWithMessage(activity, key, action, postAction, false);
		} else {
			action.run();
		}
	}

	@Override
	protected void processException(Exception exception) {
		exception.printStackTrace();

		final Activity activity = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getActivity();
		final Intent intent = new Intent(
			"android.fbreader.action.ERROR",
			new Uri.Builder().scheme(exception.getClass().getSimpleName()).build()
		);
		intent.putExtra("message", exception.getMessage());
		/*
		if (exception instanceof BookReadingException) {
			final ZLFile file = ((BookReadingException)exception).File;
			if (file != null) {
				intent.putExtra("file", file.getPath());
			}
		}
		*/
		try {
			activity.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// ignore
			e.printStackTrace();
		}
	}

	@Override
	public void setTitle(final String title) {
		final Activity activity = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					activity.setTitle(title);
				}
			});
		}
	}

	protected ZLViewWidget getViewWidget() {
		return ((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
	}

	public void close() {
		((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).finish();
	}

	private int myBatteryLevel;
	protected int getBatteryLevel() {
		return myBatteryLevel;
	}
	public void setBatteryLevel(int percent) {
		myBatteryLevel = percent;
	}
}
