package me.fragment.smartinv.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.fragment.smartinv.ClickableItem;
import me.fragment.smartinv.SmartInventory;
import me.fragment.smartinv.content.SlotIterator.Type;

public interface InventoryContents {

	SmartInventory inventory();

	Pagination addPagination();

	Pagination pagination();

	Pagination pagination(int index);

	List<Pagination> paginations();

	Optional<SlotIterator> iterator(String id);

	SlotIterator newIterator(String id, SlotIterator.Type type, int startRow, int startColumn);

	SlotIterator newIterator(SlotIterator.Type type, int startRow, int startColumn);

	SlotIterator newIterator(String id, SlotIterator.Type type, SlotPos startPos);

	SlotIterator newIterator(SlotIterator.Type type, SlotPos startPos);

	SlotIterator newIterator(SlotIterator.Type type, ListIterator<SlotPos> slots);

	SlotIterator newIterator(SlotIterator.Type type, Map<Integer, ListIterator<SlotPos>> slots);

	ClickableItem[][] all();

	Optional<SlotPos> firstEmpty();

	Optional<ClickableItem> get(int row, int column);

	Optional<ClickableItem> get(SlotPos slotPos);

	InventoryContents set(int row, int column, ClickableItem item);

	InventoryContents set(SlotPos slotPos, ClickableItem item);

	InventoryContents add(ClickableItem item);

	InventoryContents fill(ClickableItem item);

	InventoryContents fillRow(int row, ClickableItem item);

	InventoryContents fillColumn(int column, ClickableItem item);

	InventoryContents fillBorders(ClickableItem item);

	InventoryContents fillRect(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item);

	InventoryContents fillRect(SlotPos fromPos, SlotPos toPos, ClickableItem item);

	InventoryContents fillArea(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item);

	InventoryContents fillArea(SlotPos fromPos, SlotPos toPos, ClickableItem item);

	<T> T property(String name);

	<T> T property(String name, T def);

	InventoryContents setProperty(String name, Object value);

	class Impl implements InventoryContents {

		private SmartInventory inv;
		private Player player;

		private ClickableItem[][] contents;

		private List<Pagination> paginations = new ArrayList<>();
		private Map<String, SlotIterator> iterators = new HashMap<>();
		private Map<String, Object> properties = new HashMap<>();

		public Impl(SmartInventory inv, Player player) {
			this.inv = inv;
			this.player = player;
			this.contents = new ClickableItem[inv.getRows()][inv.getColumns()];
		}

		@Override
		public SmartInventory inventory() {
			return inv;
		}

		@Override
		public Pagination addPagination() {
			return this.pagination(this.paginations.size());
		}

		@Override
		public Pagination pagination() {
			return pagination(0);
		}

		@Override
		public Pagination pagination(int index) {
			if (this.paginations.size() <= index) {
				this.paginations.add(new Pagination.Impl());
			}

			return this.paginations.get(index);
		}

		@Override
		public List<Pagination> paginations() {
			return this.paginations;
		}

		@Override
		public Optional<SlotIterator> iterator(String id) {
			return Optional.ofNullable(this.iterators.get(id));
		}

		@Override
		public SlotIterator newIterator(String id, SlotIterator.Type type, int startRow, int startColumn) {
			SlotIterator iterator = new SlotIterator.Impl(this, inv, type, startRow, startColumn);

			this.iterators.put(id, iterator);
			return iterator;
		}

		@Override
		public SlotIterator newIterator(String id, SlotIterator.Type type, SlotPos startPos) {
			return newIterator(id, type, startPos.getRow(), startPos.getColumn());
		}

		@Override
		public SlotIterator newIterator(SlotIterator.Type type, int startRow, int startColumn) {
			return new SlotIterator.Impl(this, inv, type, startRow, startColumn);
		}

		@Override
		public SlotIterator newIterator(SlotIterator.Type type, SlotPos startPos) {
			return newIterator(type, startPos.getRow(), startPos.getColumn());
		}

		@Override
		public SlotIterator newIterator(SlotIterator.Type type, ListIterator<SlotPos> slots) {
			return new SlotIterator.Impl(this, inv, type, slots);
		}

		@Override
		public SlotIterator newIterator(Type type, Map<Integer, ListIterator<SlotPos>> slots) {
			return new SlotIterator.Impl(this, inv, type, slots);
		}

		@Override
		public ClickableItem[][] all() {
			return contents;
		}

		@Override
		public Optional<SlotPos> firstEmpty() {
			for (int row = 0; row < contents.length; row++) {
				for (int column = 0; column < contents[0].length; column++) {
					if (!this.get(row, column).isPresent())
						return Optional.of(new SlotPos(row, column));
				}
			}

			return Optional.empty();
		}

		@Override
		public Optional<ClickableItem> get(int row, int column) {
			if (row >= contents.length)
				return Optional.empty();
			if (column >= contents[row].length)
				return Optional.empty();

			return Optional.ofNullable(contents[row][column]);
		}

		@Override
		public Optional<ClickableItem> get(SlotPos slotPos) {
			return get(slotPos.getRow(), slotPos.getColumn());
		}

		@Override
		public InventoryContents set(int row, int column, ClickableItem item) {
			if (row >= contents.length)
				return this;
			if (column >= contents[row].length)
				return this;

			contents[row][column] = item;
			update(row, column, item != null ? item.getItem() : null);
			return this;
		}

		@Override
		public InventoryContents set(SlotPos slotPos, ClickableItem item) {
			return set(slotPos.getRow(), slotPos.getColumn(), item);
		}

		@Override
		public InventoryContents add(ClickableItem item) {
			for (int row = 0; row < contents.length; row++) {
				for (int column = 0; column < contents[0].length; column++) {
					if (contents[row][column] == null) {
						set(row, column, item);
						return this;
					}
				}
			}

			return this;
		}

		@Override
		public InventoryContents fill(ClickableItem item) {
			for (int row = 0; row < contents.length; row++)
				for (int column = 0; column < contents[row].length; column++)
					set(row, column, item);

			return this;
		}

		@Override
		public InventoryContents fillRow(int row, ClickableItem item) {
			if (row >= contents.length)
				return this;

			for (int column = 0; column < contents[row].length; column++)
				set(row, column, item);

			return this;
		}

		@Override
		public InventoryContents fillColumn(int column, ClickableItem item) {
			for (int row = 0; row < contents.length; row++)
				set(row, column, item);

			return this;
		}

		@Override
		public InventoryContents fillBorders(ClickableItem item) {
			fillRect(0, 0, inv.getRows() - 1, inv.getColumns() - 1, item);
			return this;
		}

		@Override
		public InventoryContents fillRect(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item) {
			for (int row = fromRow; row <= toRow; row++) {
				for (int column = fromColumn; column <= toColumn; column++) {
					if (row != fromRow && row != toRow && column != fromColumn && column != toColumn)
						continue;

					set(row, column, item);
				}
			}

			return this;
		}

		@Override
		public InventoryContents fillRect(SlotPos fromPos, SlotPos toPos, ClickableItem item) {
			return fillRect(fromPos.getRow(), fromPos.getColumn(), toPos.getRow(), toPos.getColumn(), item);
		}

		@Override
		public InventoryContents fillArea(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item) {
			for (int row = fromRow; row <= toRow; row++) {
				for (int column = fromColumn; column <= toColumn; column++) {
					set(row, column, item);
				}
			}

			return this;
		}

		@Override
		public InventoryContents fillArea(SlotPos fromPos, SlotPos toPos, ClickableItem item) {
			return fillArea(fromPos.getRow(), fromPos.getColumn(), toPos.getRow(), toPos.getColumn(), item);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T property(String name) {
			return (T) properties.get(name);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T property(String name, T def) {
			return properties.containsKey(name) ? (T) properties.get(name) : def;
		}

		@Override
		public InventoryContents setProperty(String name, Object value) {
			properties.put(name, value);
			return this;
		}

		private void update(int row, int column, ItemStack item) {
			if (!inv.getManager().getOpenedPlayers(inv).contains(player))
				return;

			Inventory topInventory = player.getOpenInventory().getTopInventory();
			topInventory.setItem(inv.getColumns() * row + column, item);
		}

	}

}