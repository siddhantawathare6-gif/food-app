INSERT IGNORE INTO foodcataloguedb.food_item (id, is_veg, quantity, restaurant_id, price, item_description, item_name) VALUES
-- Restaurant 1: Ravi Kulkarni (Pune)
(1, 1, 4, 1, 350, 'Classic wood-fired margherita with basil and mozzarella', 'Margherita Pizza'),
(2, 1, 2, 1, 180, 'Freshly brewed single-origin filter coffee', 'Craft Coffee'),
(3, 0, 5, 1, 420, 'Wood-fired pepperoni pizza with smoked meats', 'Pepperoni Pizza'),

-- Restaurant 2: Anjali Mehta (Kolkata)
(4, 0, 3, 2, 280, 'Traditional Bengali fish curry cooked in mustard gravy', 'Macher Jhol'),
(5, 0, 1, 2, 240, 'Slow-cooked mutton curry with Bengali spices', 'Kosha Mangsho'),
(6, 1, 4, 2, 150, 'Steamed rice dumplings with coconut filling', 'Pati Shapta'),

-- Restaurant 3: Karan Verma (Chandigarh)
(7, 1, 3, 3, 220, 'Fusion paneer tikka wrap with continental slaw', 'Paneer Fusion Wrap'),
(8, 0, 2, 3, 260, 'Grilled chicken with Punjabi-continental glaze', 'Fusion Chicken Grill'),

-- Restaurant 4: Priya Nair (Kochi)
(9, 0, 5, 4, 400, 'Kerala-style seafood platter with coconut curry', 'Seafood Platter'),
(10, 0, 2, 4, 180, 'Pan-fried pearl spot fish in banana leaf', 'Karimeen Fry'),
(11, 1, 4, 4, 90, 'Steamed rice cakes served with vegetable stew', 'Appam with Stew'),

-- Restaurant 5: Aditya Rao (Hyderabad)
(12, 0, 1, 5, 320, 'Slow-cooked dum biryani with tender mutton', 'Mutton Dum Biryani'),
(13, 0, 3, 5, 280, 'Hyderabadi chicken biryani with saffron rice', 'Chicken Dum Biryani'),
(14, 0, 5, 5, 150, 'Skewered spiced kebabs grilled over charcoal', 'Seekh Kebab'),

-- Restaurant 6: Sneha Joshi (Pune)
(15, 1, 5, 6, 180, 'Unlimited Maharashtrian thali with seasonal vegetables', 'Veg Thali'),
(16, 1, 3, 6, 60, 'Spiced mashed potato patty in a soft bun', 'Vada Pav'),

-- Restaurant 7: Vikram Singh (Jaipur)
(17, 1, 2, 7, 250, 'Royal Rajasthani dal baati churma', 'Dal Baati Churma'),
(18, 1, 4, 7, 80, 'Chilled yogurt-based sweet lassi', 'Sweet Lassi'),
(19, 1, 1, 7, 200, 'Deep-fried Rajasthani vegetable dumplings in gravy', 'Gatte ki Sabzi'),

-- Restaurant 8: Neha Kapoor (Mumbai)
(20, 1, 5, 8, 60, 'Spiced potato fritter in a soft bun with chutneys', 'Vada Pav'),
(21, 1, 3, 8, 90, 'Spicy mashed street-food curry with pav bread', 'Pav Bhaji'),

-- Restaurant 9: Arjun Desai (Ahmedabad)
(22, 1, 4, 9, 120, 'Crispy Gujarati dhokla steamed and tempered', 'Dhokla'),
(23, 1, 2, 9, 100, 'Assorted Gujarati farsan snack platter', 'Farsan Platter'),
(24, 1, 5, 9, 140, 'Sweet and tangy Gujarati thali combo', 'Gujarati Thali'),

-- Restaurant 10: Meera Iyer (Chennai)
(25, 1, 3, 10, 90, 'Crispy South Indian dosa with coconut chutney and sambar', 'Masala Dosa'),
(26, 1, 1, 10, 70, 'Steamed rice cake served with sambar and chutney', 'Idli Sambar'),
(27, 1, 4, 10, 50, 'Strong South Indian filter coffee', 'Filter Coffee');
