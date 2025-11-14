package com.example.mixmate.notifications

/**
 * Repository of fun cocktail facts for daily notifications
 */
object CocktailFacts {
    private val facts = listOf(
        "The Margarita is the most popular cocktail in America! 🍹",
        "The word 'cocktail' first appeared in print in 1806 in a New York newspaper.",
        "The Mojito was Ernest Hemingway's favorite cocktail! 🌿",
        "A standard shot is 1.5 ounces, but different countries have different standards.",
        "The Old Fashioned is considered the original cocktail, dating back to the 1800s.",
        "The Cosmopolitan became popular in the 1990s thanks to 'Sex and the City' 📺",
        "The Mai Tai was created in Oakland, California in 1944, not in Hawaii!",
        "Gin was originally used as medicine in the 17th century.",
        "The Moscow Mule is traditionally served in a copper mug to keep it cold. 🥃",
        "James Bond's famous 'shaken, not stirred' actually bruises the gin!",
        "The Bloody Mary is named after Queen Mary I of England.",
        "A 'neat' drink is served at room temperature with no ice or mixers.",
        "The Piña Colada is the national drink of Puerto Rico since 1978! 🍍",
        "Tequila can only be called tequila if it's made in specific regions of Mexico.",
        "The Daiquiri was supposedly invented by an American mining engineer in Cuba.",
        "Champagne can only be called champagne if it comes from the Champagne region of France. 🥂",
        "The Manhattan cocktail is over 150 years old!",
        "A 'dirty' martini includes olive brine for a savory twist.",
        "The Negroni was invented in Florence, Italy in 1919.",
        "Whiskey gets its color from the wooden barrels it's aged in, not from additives.",
        "The Tom Collins was named after a popular 19th-century practical joke!",
        "Rum was once used as currency in Australia! 💰",
        "The Espresso Martini was invented in London in the 1980s.",
        "Absinthe was banned in many countries but is now legal again in most places.",
        "The Sazerac is the official cocktail of New Orleans. 🎺",
        "A 'dry' cocktail contains less vermouth than a regular one.",
        "The Long Island Iced Tea contains no tea at all!",
        "Vodka is the most mixable spirit and works with almost any flavor.",
        "The Aperol Spritz originated in Venice, Italy. 🇮🇹",
        "Bitters were originally sold as patent medicines!",
        "The French 75 is named after a WWI field gun due to its kick. 💥",
        "Sake is technically a beer, not a wine, because it's brewed from rice.",
        "The Gimlet was supposedly created to help British sailors fight scurvy.",
        "Tiki cocktails were popularized in 1930s America, creating 'Polynesian' themed bars.",
        "The Mint Julep is the traditional drink of the Kentucky Derby. 🐎",
        "Cocktail shakers come in two main styles: Boston and Cobbler.",
        "The Dark 'n' Stormy is a trademarked cocktail owned by Gosling's Rum!",
        "A muddler is used to crush herbs, fruits, and sugar in cocktails.",
        "The Aviation cocktail gets its blue color from crème de violette.",
        "Prohibition in the US (1920-1933) actually led to the creation of many classic cocktails! 🚫",
        "Simple syrup is just equal parts sugar and water, dissolved together.",
        "The Sidecar cocktail was invented in a Parisian bar during WWI.",
        "Angostura bitters were created in Venezuela as a medicinal tonic.",
        "The Vesper martini was invented by Ian Fleming for James Bond.",
        "A 'twist' garnish is a strip of citrus peel that adds aroma to your drink. 🍋",
        "The Caipirinha is Brazil's national cocktail, made with cachaça.",
        "Champagne should be poured in two stages to prevent overflow!",
        "The word 'bar' comes from the barrier separating bartenders from customers.",
        "A 'float' is when you gently pour a spirit on top of a drink so it sits on the surface.",
        "The Bellini was invented at Harry's Bar in Venice in the 1930s. 🍑"
    )

    /**
     * Get a random cocktail fact
     */
    fun getRandomFact(): String {
        return facts.random()
    }

    /**
     * Get all facts
     */
    fun getAllFacts(): List<String> {
        return facts
    }
}

