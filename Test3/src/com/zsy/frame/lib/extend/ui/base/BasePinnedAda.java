package com.zsy.frame.lib.extend.ui.base;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.pinnedheader.PinnedHeaderListViewConfig;
import com.handmark.pulltorefresh.library.pinnedheader.PinnedHeaderStateListener;

public abstract class BasePinnedAda<T> extends BaseAda<T> implements PinnedHeaderStateListener {
	public BasePinnedAda(Context context) {
		super(context);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (view instanceof PinnedHeaderListViewConfig) {
			if (view instanceof ListView) {
				firstVisibleItem = firstVisibleItem - ((ListView) view).getHeaderViewsCount();
			}
			((PinnedHeaderListViewConfig) view).configureHeaderView(firstVisibleItem);
		}
	}

	/**
	 * Configures the pinned header view to match the first visible list item.
	 * 配置悬浮头
	 * 
	 * @param header
	 *            pinned header view.
	 * @param position
	 *            position of the first visible list item.
	 * @param alpha
	 *            fading of the header view, between 0 and 255.
	 */
	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
	}

	@Override
	public int getPinnedHeaderState(int position) {
		if (getCount() == 0) { return PINNED_HEADER_GONE; }

		if (position < 0 || position > getCount()) { return PINNED_HEADER_GONE; }

		if (isPushUp(position)) { return PINNED_HEADER_PUSHED_UP; }

		return PINNED_HEADER_VISIBLE;
	}

	/**
	 * @description：判断指定位置是否需要上推
	 * @author samy
	 * @date 2015-1-23 上午12:33:45
	 */
	protected boolean isPushUp(int position) {
		return false;
	}
}
