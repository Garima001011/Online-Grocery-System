loadCategories();
loadProducts();

function loadCategories() {
    fetch('/api/categories')
        .then(r => r.json())
        .then(data => {
            const list = document.getElementById('categoryList');
            const select = document.getElementById('categorySelect');
            list.innerHTML = '';
            select.innerHTML = '';
            data.forEach(c => {
                const li = document.createElement('li');
                li.innerText = c.name;
                list.appendChild(li);
                const opt = document.createElement('option');
                opt.value = c.id;
                opt.text = c.name;
                select.appendChild(opt);
            });
        });
}

function loadProducts() {
    fetch('/api/products')
        .then(r => r.json())
        .then(data => {
            const list = document.getElementById('productList');
            list.innerHTML = '';
            data.forEach(p => {
                const li = document.createElement('li');
                const price = new Intl.NumberFormat('en-NP').format(p.price);
                li.innerText = `${p.name} | Rs. ${price} | Stock: ${p.stock} | ${p.category.name}`;
                list.appendChild(li);
            });
        });
}

function addCategory() {
    const name = document.getElementById('categoryName').value;
    fetch('/api/categories', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name })
    }).then(() => {
        document.getElementById('categoryName').value = '';
        loadCategories();
    });
}

function addProduct() {
    const name = document.getElementById('productName').value;
    const price = document.getElementById('productPrice').value;
    const stock = document.getElementById('productStock').value;
    const categoryId = document.getElementById('categorySelect').value;
    fetch('/api/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, price, stock, categoryId })
    }).then(() => {
        loadProducts();
    });
}