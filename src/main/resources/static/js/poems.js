document.addEventListener('DOMContentLoaded', () => {
    const poemsContainer = document.querySelector('.poems-container');
    if (!poemsContainer) return;

    const poems = Array.from(poemsContainer.querySelectorAll('.poem-card'));

    const categoryFilter = document.getElementById('category-filter');
    const sortFilter = document.getElementById('sort-filter');
    const searchInput = document.getElementById('poem-search');
    const searchButton = document.querySelector('.filter-group button');

    function filterAndSortPoems() {
        const category = categoryFilter.value;
        const sortBy = sortFilter.value;
        const searchTerm = searchInput.value.toLowerCase();

        let filteredPoems = poems;

        // Filter by category
        if (category) {
            filteredPoems = filteredPoems.filter(poem => {
                const tags = Array.from(poem.querySelectorAll('.poem-tag')).map(tag => tag.textContent.toLowerCase());
                return tags.includes(category.toLowerCase());
            });
        }

        // Filter by search term
        if (searchTerm) {
            filteredPoems = filteredPoems.filter(poem => {
                const title = poem.querySelector('h3').textContent.toLowerCase();
                const author = poem.querySelector('.name').textContent.toLowerCase();
                const excerpt = poem.querySelector('.poem-excerpt').textContent.toLowerCase();
                return title.includes(searchTerm) || author.includes(searchTerm) || excerpt.includes(searchTerm);
            });
        }

        // Sort poems
        if (sortBy === 'popular') {
            filteredPoems.sort((a, b) => {
                const likesA = parseInt(a.querySelector('.fa-heart').nextSibling.textContent.trim());
                const likesB = parseInt(b.querySelector('.fa-heart').nextSibling.textContent.trim());
                return likesB - likesA;
            });
        } else if (sortBy === 'trending') {
            // Simple trending: combination of likes and comments
            filteredPoems.sort((a, b) => {
                const scoreA = parseInt(a.querySelector('.fa-heart').nextSibling.textContent.trim()) + parseInt(a.querySelector('.fa-comment').nextSibling.textContent.trim());
                const scoreB = parseInt(b.querySelector('.fa-heart').nextSibling.textContent.trim()) + parseInt(b.querySelector('.fa-comment').nextSibling.textContent.trim());
                return scoreB - scoreA;
            });
        } else { // newest
            filteredPoems.sort((a, b) => {
                const dateA = new Date(a.querySelector('.date').textContent);
                const dateB = new Date(b.querySelector('.date').textContent);
                return dateB - dateA; // This will not work well with "2 days ago"
            });
        }

        // Display filtered poems
        poemsContainer.innerHTML = '';
        if (filteredPoems.length > 0) {
            filteredPoems.forEach(poem => poemsContainer.appendChild(poem));
        } else {
            poemsContainer.innerHTML = '<p>No poems match your criteria.</p>';
        }
    }

    categoryFilter.addEventListener('change', filterAndSortPoems);
    sortFilter.addEventListener('change', filterAndSortPoems);
    searchButton.addEventListener('click', filterAndSortPoems);
    searchInput.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') {
            filterAndSortPoems();
        }
    });

    // Responsive filter toggle
    const filterToggle = document.querySelector('.filter-toggle');
    const filterOptions = document.querySelector('.filter-options');
    if(filterToggle && filterOptions) {
        filterToggle.addEventListener('click', () => {
            filterOptions.classList.toggle('active');
        });
    }
});
