package com.lodging.Restarurant.controller;

import com.lodging.Restarurant.model.User;
import com.lodging.Restarurant.model.enums.RoomOrderBillingType;
import com.lodging.Restarurant.service.RoomOrderService;
import com.lodging.Restarurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class RestaurantController {

    private final UserService userService;
    private final RoomOrderService roomOrderService;

    private static final Map<String, Venue> VENUES = buildVenues();

    @GetMapping("/restaurant")
    public String restaurantHome(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("venues", VENUES.values());
        model.addAttribute("isCustomer", isCustomer(userDetails));
        return "restaurant/index";
    }

    @GetMapping("/restaurant/{slug}")
    public String venuePage(@PathVariable String slug,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        Venue venue = VENUES.get(slug);
        if (venue == null) {
            return "redirect:/restaurant";
        }
        model.addAttribute("venue", venue);
        model.addAttribute("isCustomer", isCustomer(userDetails));
        model.addAttribute("reservation", new ReservationForm());
        model.addAttribute("foodOrder", new FoodOrderForm());
        return "restaurant/venue";
    }

    @PostMapping("/customer/restaurant-reservations/{slug}")
    public String reserve(@PathVariable String slug,
                          @ModelAttribute("reservation") ReservationForm reservation,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes flash) {
        Venue venue = VENUES.get(slug);
        if (venue == null) {
            flash.addFlashAttribute("errorMsg", "Selected venue is not available.");
            return "redirect:/restaurant";
        }
        if (userDetails == null) {
            flash.addFlashAttribute("errorMsg", "Please log in or create an account to reserve a table.");
            return "redirect:/auth/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        if (!user.isCustomer()) {
            flash.addFlashAttribute("errorMsg", "Table reservations are currently available for hotel customers.");
            return "redirect:/restaurant/" + slug;
        }

        flash.addFlashAttribute("successMsg",
                "Reservation request received for " + venue.name() + " on " + reservation.date
                        + " at " + reservation.time + " for " + reservation.guests + " guest(s).");
        return "redirect:/restaurant/" + slug;
    }

    @PostMapping("/customer/restaurant-orders/{slug}")
    public String placeFoodOrder(@PathVariable String slug,
                                 @ModelAttribute("foodOrder") FoodOrderForm form,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes flash) {
        Venue venue = VENUES.get(slug);
        if (venue == null) {
            flash.addFlashAttribute("errorMsg", "Selected venue is not available.");
            return "redirect:/restaurant";
        }
        if (userDetails == null) {
            flash.addFlashAttribute("errorMsg", "Please log in or create an account to place an order.");
            return "redirect:/auth/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        if (!user.isCustomer()) {
            flash.addFlashAttribute("errorMsg", "Only hotel customers can place in-room dining orders.");
            return "redirect:/restaurant/" + slug;
        }

        try {
            RoomOrderBillingType billingType = RoomOrderBillingType.valueOf(form.getBillingType());
            roomOrderService.placeCustomerOrder(
                    user,
                    venue.slug(),
                    venue.name(),
                    form.getItemName(),
                    form.getItemDescription(),
                    new BigDecimal(form.getItemPrice()),
                    form.getQuantity(),
                    billingType
            );
            flash.addFlashAttribute("successMsg",
                    "Order placed successfully. Billing mode: " + billingType.name().replace('_', ' '));
        } catch (Exception e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/restaurant/" + slug;
    }

    private boolean isCustomer(UserDetails userDetails) {
        if (userDetails == null) return false;
        User user = userService.findByEmail(userDetails.getUsername());
        return user.isCustomer();
    }

    private static Map<String, Venue> buildVenues() {
        Map<String, Venue> venues = new LinkedHashMap<>();

        venues.put("saffron-sunrise-cafe", new Venue(
                "saffron-sunrise-cafe",
                "Saffron Sunrise Cafe",
                "A sunlit breakfast atelier with artisanal coffee and fresh bakery classics.",
                "Contemporary cafe / breakfast bar",
                "6:30 AM - 11:30 AM",
                "https://images.unsplash.com/photo-1554118811-1e0d58224f24",
                "Fragrant coffee, warm ovens, and an easy luxury start to your day define Saffron Sunrise Cafe. " +
                        "The venue blends modern cafe design with plush seating and garden-facing windows for calm morning dining.",
                List.of(
                        new MenuSection("Breakfast Signatures", List.of(
                                new MenuItem("Truffle Masala Omelette", "Free-range eggs, truffle butter, sourdough toast.", "₹650",
                                        "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=600"),
                                new MenuItem("Lotus Pancake Stack", "Fluffy pancakes, berry compote, vanilla creme.", "₹720",
                                        "https://images.unsplash.com/photo-1528207776546-365bb710ee93?w=600"),
                                new MenuItem("Mediterranean Avocado Bowl", "Poached eggs, avocado, confit tomatoes, feta.", "₹690",
                                        "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600")
                        )),
                        new MenuSection("Coffee & Tea", List.of(
                                new MenuItem("Single Estate Pour Over", "Estate beans with floral finish.", "₹410",
                                        "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600"),
                                new MenuItem("Saffron Cardamom Latte", "Silky milk, saffron strands, cardamom dust.", "₹460",
                                        "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600"),
                                new MenuItem("Vintage Masala Chai", "Spiced Assam tea, jaggery caramel note.", "₹290",
                                        "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600")
                        )),
                        new MenuSection("Fresh Juices", List.of(
                                new MenuItem("Emerald Detox", "Celery, apple, cucumber, mint.", "₹350",
                                        "https://images.unsplash.com/photo-1610970881699-44a5587cabec?w=600"),
                                new MenuItem("Mango Sunrise", "Alphonso mango, orange, lime.", "₹390",
                                        "https://images.unsplash.com/photo-1600271886742-f049cd5bba3f?w=600"),
                                new MenuItem("Ruby Press", "Watermelon, basil, pink salt.", "₹330",
                                        "https://images.unsplash.com/photo-1610970881699-44a5587cabec?w=600")
                        ))
                )));

        venues.put("azure-flame-grill", new Venue(
                "azure-flame-grill",
                "Azure Flame Grill",
                "Signature fine dining with coastal global cuisine and chef tasting menus.",
                "Fine dining / chef-led experience",
                "7:00 PM - 11:30 PM",
                "https://images.unsplash.com/photo-1559339352-11d035aa65de",
                "Azure Flame Grill is our statement dining room with dramatic lighting, velvet textures, and curated music. " +
                        "Expect refined plating, charcoal aromas, and attentive table service for celebratory evenings.",
                List.of(
                        new MenuSection("Starters", List.of(
                                new MenuItem("Charred Burrata", "Heirloom tomato jam, basil oil, sour crackers.", "₹980",
                                        "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=600"),
                                new MenuItem("Tandoori Lobster Bites", "Smoked chili glaze, citrus aioli.", "₹1450",
                                        "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=600"),
                                new MenuItem("Wild Mushroom Galette", "Puff pastry, porcini cream, parmesan snow.", "₹920",
                                        "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=600")
                        )),
                        new MenuSection("Mains", List.of(
                                new MenuItem("Saffron Sea Bass", "Pan-seared bass, confit fennel, beurre blanc.", "₹1890",
                                        "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=600"),
                                new MenuItem("Lamb Wellington", "Herb lamb, truffle mash, red wine jus.", "₹2250",
                                        "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=600"),
                                new MenuItem("Forest Truffle Risotto", "Arborio, parmesan, black truffle shaving.", "₹1560",
                                        "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=600")
                        )),
                        new MenuSection("Desserts", List.of(
                                new MenuItem("Dark Chocolate Dome", "Hazelnut praline core, cocoa crumble.", "₹690",
                                        "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=600"),
                                new MenuItem("Rose Pistachio Opera", "Layered sponge, rose cream, pistachio dust.", "₹640",
                                        "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=600"),
                                new MenuItem("Citrus Vanilla Mille-Feuille", "Caramelized puff, custard, citrus pearls.", "₹620",
                                        "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=600")
                        ))
                )));

        venues.put("moonlit-oasis-bar", new Venue(
                "moonlit-oasis-bar",
                "Moonlit Oasis Bar",
                "A rooftop lounge for crafted cocktails, mocktails, and elevated bar bites.",
                "Rooftop lounge / cocktail bar",
                "5:00 PM - 1:00 AM",
                "https://images.unsplash.com/photo-1514933651103-005eec06c04b",
                "Moonlit Oasis Bar offers skyline views, ambient jazz, and a polished marble bar. " +
                        "The mood transitions from sunset mocktails to lively late-night signatures.",
                List.of(
                        new MenuSection("Mocktails", List.of(
                                new MenuItem("Sundown Serenade", "Passionfruit, basil, ginger ale.", "₹520",
                                        "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=600"),
                                new MenuItem("Blue Pearl Fizz", "Blue pea tea, citrus, tonic, mint smoke.", "₹560",
                                        "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=600"),
                                new MenuItem("Coco Mojito Zero", "Tender coconut, lime, mint, soda.", "₹510",
                                        "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=600")
                        )),
                        new MenuSection("Cocktails", List.of(
                                new MenuItem("Saffron Negroni", "Saffron infused gin, bitters, vermouth.", "₹890",
                                        "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=600"),
                                new MenuItem("Smoked Tamarind Old Fashioned", "Bourbon, tamarind reduction, oak smoke.", "₹940",
                                        "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=600"),
                                new MenuItem("Velvet Espresso Martini", "Cold brew, vodka, coffee liqueur foam.", "₹920",
                                        "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600")
                        )),
                        new MenuSection("Bar Snacks", List.of(
                                new MenuItem("Crispy Lotus Stem", "Chili honey glaze, toasted sesame.", "₹540",
                                        "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=600"),
                                new MenuItem("Pepper Chicken Sliders", "Brioche buns, chipotle mayo, pickles.", "₹690",
                                        "https://images.unsplash.com/photo-1550547660-d9450f859349?w=600"),
                                new MenuItem("Truffle Parmesan Fries", "Hand-cut fries, aged parmesan, truffle dust.", "₹590",
                                        "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=600")
                        ))
                )));

        return venues;
    }

    public static record Venue(
            String slug,
            String name,
            String shortDescription,
            String cuisineType,
            String operatingHours,
            String coverImage,
            String fullDescription,
            List<MenuSection> menuSections
    ) {}

    public static record MenuSection(String name, List<MenuItem> items) {}

    public static record MenuItem(String name, String description, String price, String imageUrl) {}

    public static class ReservationForm {
        private String date;
        private String time;
        private int guests = 2;
        private String specialRequest;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public int getGuests() {
            return guests;
        }

        public void setGuests(int guests) {
            this.guests = guests;
        }

        public String getSpecialRequest() {
            return specialRequest;
        }

        public void setSpecialRequest(String specialRequest) {
            this.specialRequest = specialRequest;
        }
    }

    public static class FoodOrderForm {
        private String itemName;
        private String itemDescription;
        private String itemPrice;
        private int quantity = 1;
        private String billingType;

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getItemDescription() {
            return itemDescription;
        }

        public void setItemDescription(String itemDescription) {
            this.itemDescription = itemDescription;
        }

        public String getItemPrice() {
            return itemPrice;
        }

        public void setItemPrice(String itemPrice) {
            this.itemPrice = itemPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getBillingType() {
            return billingType;
        }

        public void setBillingType(String billingType) {
            this.billingType = billingType;
        }
    }
}
