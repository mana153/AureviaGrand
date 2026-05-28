(function () {
    const FALLBACKS = {
        breakfast: 'https://images.unsplash.com/photo-1525351484163-7529414344d8?w=600',
        pancakes: 'https://images.unsplash.com/photo-1528207776546-365bb710ee93?w=600',
        salad: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600',
        coffee: 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600',
        dining: 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=600',
        mocktails: 'https://images.unsplash.com/photo-1544145945-f90425340c7e?w=600',
        desserts: 'https://images.unsplash.com/photo-1551024601-bec78aea704b?w=600',
        seafood: 'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=600',
        juice: 'https://images.unsplash.com/photo-1610970881699-44a5587cabec?w=600'
    };

    window.fallbackMenuImage = function (img) {
        const fb = img.dataset.fallback;
        if (fb) {
            img.src = fb;
            return;
        }
        const alt = (img.alt || '').toLowerCase();
        const section = (img.closest('[data-menu-section]')?.dataset.menuSection || '').toLowerCase();
        if (alt.includes('pancake')) img.src = FALLBACKS.pancakes;
        else if (alt.includes('omelette') || alt.includes('egg') || section.includes('breakfast')) img.src = FALLBACKS.breakfast;
        else if (alt.includes('bowl') || alt.includes('avocado') || alt.includes('salad')) img.src = FALLBACKS.salad;
        else if (alt.includes('coffee') || alt.includes('latte') || alt.includes('chai') || alt.includes('pour')) img.src = FALLBACKS.coffee;
        else if (alt.includes('mocktail') || alt.includes('mojito') || alt.includes('fizz') || section.includes('mocktail') || section.includes('cocktail')) img.src = FALLBACKS.mocktails;
        else if (alt.includes('dessert') || alt.includes('chocolate') || alt.includes('opera') || alt.includes('mille')) img.src = FALLBACKS.desserts;
        else if (alt.includes('lobster') || alt.includes('bass') || alt.includes('fish')) img.src = FALLBACKS.seafood;
        else if (alt.includes('juice') || alt.includes('detox') || alt.includes('mango')) img.src = FALLBACKS.juice;
        else img.src = FALLBACKS.dining;
    };

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.menu-item-image').forEach((img) => {
            img.addEventListener('error', function () {
                if (img.dataset.fallbackApplied === '1') return;
                img.dataset.fallbackApplied = '1';
                window.fallbackMenuImage(img);
            });
        });
    });
})();
