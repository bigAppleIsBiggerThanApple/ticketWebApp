package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<>();
		// Step 1, given a user, fetch all the events (ids) this user has visited.
		DBConnection connection = DBConnectionFactory.getConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);

		// Step 2, given all these events, fetch all categories
		// {music: 5, art: 3, sports: 1}
		Map<String, Integer> allCategories = new HashMap<>();
		for (String favoritedItemId : favoritedItemIds) {
			Set<String> categories = connection.getCategories(favoritedItemId);
			for (String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		Collections.sort(categoryList, (Entry<String, Integer> o1, Entry<String, Integer> o2) -> {
			return Integer.compare(o2.getValue(), o1.getValue());
		});
		// Step 3, given these categories, what are the events that belong to them
		// {music: 5, art: 3, sports: 1}
		Set<String> visitedItemIds = new HashSet<>();
		for (Entry<String, Integer> category : categoryList) {
			List<Item> items = connection.searchItems(lat, lon, category.getKey());
			for (Item item : items) {
				if (!favoritedItemIds.contains(item.getItemId()) && !visitedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item);
					visitedItemIds.add(item.getItemId());
				}
			}
		}

		connection.close();

		return recommendedItems;

	}

}
