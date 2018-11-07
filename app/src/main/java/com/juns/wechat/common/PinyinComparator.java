package com.juns.wechat.common;

import java.util.Comparator;

import com.data.db.Friend;
import com.juns.wechat.bean.User;

public class PinyinComparator implements Comparator {

	@Override
	public int compare(Object arg0, Object arg1) {
		// 按照名字排序
		Friend user0 = (Friend) arg0;
		Friend user1 = (Friend) arg1;
		String catalog0 = "";
		String catalog1 = "";

		if (user0 != null && user0.getName() != null
				&& user0.getName().length() > 1)
			catalog0 = PingYinUtil.converterToFirstSpell(user0.getName())
					.substring(0, 1);

		if (user1 != null && user1.getName() != null
				&& user1.getName().length() > 1)
			catalog1 = PingYinUtil.converterToFirstSpell(user1.getName())
					.substring(0, 1);
		int flag = catalog0.compareTo(catalog1);
		return flag;

	}

}
