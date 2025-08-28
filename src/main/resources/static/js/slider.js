// Testimonial Slider
document.addEventListener('DOMContentLoaded', function() {
    initializeSlider();
});

function initializeSlider() {
    const testimonials = document.querySelectorAll('.testimonial');
    const dots = document.querySelectorAll('.slider-dot');
    
    if (testimonials.length === 0 || dots.length === 0) return;
    
    // Set up click events for dots
    dots.forEach(dot => {
        dot.addEventListener('click', function() {
            const index = this.getAttribute('data-index');
            showTestimonial(parseInt(index));
        });
    });
    
    // Auto rotate testimonials
    let currentIndex = 0;
    const interval = setInterval(() => {
        currentIndex = (currentIndex + 1) % testimonials.length;
        showTestimonial(currentIndex);
    }, 5000);
    
    function showTestimonial(index) {
        // Hide all testimonials
        testimonials.forEach(testimonial => {
            testimonial.classList.remove('active');
        });
        
        // Remove active class from all dots
        dots.forEach(dot => {
            dot.classList.remove('active');
        });
        
        // Show the selected testimonial
        testimonials[index].classList.add('active');
        dots[index].classList.add('active');
    }
}