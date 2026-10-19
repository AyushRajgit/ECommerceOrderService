import { useEffect, useMemo, useRef, useState } from 'react';
import {
  ArrowRight,
  BookOpen,
  Check,
  ChevronDown,
  Flower2,
  Headphones,
  Home,
  Laptop,
  MapPin,
  Menu,
  PackageCheck,
  Pencil,
  Plus,
  Search,
  Shirt,
  ShoppingCart,
  Smartphone,
  Sparkles,
  Star,
  Tag,
  Trash2,
  Truck,
  UserRound,
  X,
} from 'lucide-react';

const BASE_URL = import.meta.env.BACKEND_API_URL;
const API = `${BASE_URL.replace(/\/$/, '')}/api/amazon`;
const PRODUCT_PAGE_SIZE = 10;
const EMAIL_KEY = 'shopnest.email';
const PROFILE_KEY = 'shopnest.customer';

const categories = [
  { id: 'all', label: 'All departments', Icon: Menu },
  { id: 'tech', label: 'Electronics', Icon: Laptop },
  { id: 'home', label: 'Home & kitchen', Icon: Home },
  { id: 'fashion', label: 'Fashion', Icon: Shirt },
  { id: 'books', label: 'Books', Icon: BookOpen },
  { id: 'beauty', label: 'Beauty & care', Icon: Flower2 },
];

function readProfile() {
  try {
    return JSON.parse(localStorage.getItem(PROFILE_KEY) || 'null');
  } catch {
    return null;
  }
}

function makeClientProfile(customer, fallbackEmail) {
  if (!customer?.customerId) throw new Error('The backend did not return a customer ID.');
  return {
    customerId: customer.customerId,
    fullName: [customer.firstName, customer.lastName].filter(Boolean).join(' '),
    email: customer.email || fallbackEmail,
  };
}

async function apiRequest(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(`${API}${path}`, {
    ...options,
    headers,
    credentials: 'include',
  });
  const raw = await response.text();
  let data = raw;
  if (raw) {
    try {
      data = JSON.parse(raw);
    } catch {
      // Auth endpoints return plain text; keep that response as-is.
    }
  }

  if (!response.ok) {
    const error = new Error(data?.message || data?.error || data || `Request failed (${response.status})`);
    error.status = response.status;
    throw error;
  }
  return data;
}

async function fetchProductCatalog() {
  const firstPage = await apiRequest(`/register-product?pageNo=0&pageSize=${PRODUCT_PAGE_SIZE}`);
  const pageCount = Number(firstPage.totalPages) || 0;
  const remainingPages = await Promise.all(
    Array.from({ length: Math.max(0, pageCount - 1) }, (_, index) =>
      apiRequest(`/register-product?pageNo=${index + 1}&pageSize=${PRODUCT_PAGE_SIZE}`)
    )
  );
  return [firstPage, ...remainingPages].flatMap((page) => Array.isArray(page.products) ? page.products : []);
}

function productCategory(product) {
  const text = `${product.productName || ''} ${product.productDescription || ''}`.toLowerCase();
  if (/book|novel|journal|story/.test(text)) return 'books';
  if (/beauty|skin|face|hair|makeup|serum/.test(text)) return 'beauty';
  if (/shirt|shoe|dress|wear|fashion|bag|jacket|sandal/.test(text)) return 'fashion';
  if (/phone|mobile|laptop|computer|headphone|camera|speaker|electronic|watch|tablet/.test(text)) return 'tech';
  if (/home|kitchen|lamp|chair|table|cook|bottle|mug|decor/.test(text)) return 'home';
  return 'everyday';
}

function formatPrice(value) {
  const amount = Number(value);
  if (!Number.isFinite(amount)) return '₹—';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: amount % 1 === 0 ? 0 : 2,
  }).format(amount);
}

function createEmptyManagerProduct() {
  return {
    productName: '',
    productPrice: '',
    availableQuantity: '10',
    productDescription: '',
    merchantDetail: 'Shopnest Marketplace',
  };
}

function App() {
  const [products, setProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);
  const [role, setRole] = useState(null);
  const [email, setEmail] = useState(() => localStorage.getItem(EMAIL_KEY) || '');
  const [profile, setProfile] = useState(readProfile);
  const [authOpen, setAuthOpen] = useState(() => !localStorage.getItem(EMAIL_KEY));
  const [authMode, setAuthMode] = useState('login');
  const [profileOpen, setProfileOpen] = useState(false);
  const [cartOpen, setCartOpen] = useState(false);
  const [cartItems, setCartItems] = useState([]);
  const [orders, setOrders] = useState([]);
  const [ordersOpen, setOrdersOpen] = useState(false);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState('SIMULATED_UPI');
  const [checkoutOrder, setCheckoutOrder] = useState(null);
  const [search, setSearch] = useState('');
  const [activeCategory, setActiveCategory] = useState('all');
  const [pageNo, setPageNo] = useState(0);
  const [editingProduct, setEditingProduct] = useState(null);
  const [sort, setSort] = useState('featured');
  const [toast, setToast] = useState('');
  const toastTimeout = useRef(null);

  const showToast = (message) => {
    setToast(message);
    window.clearTimeout(toastTimeout.current);
    toastTimeout.current = window.setTimeout(() => setToast(''), 3200);
  };

  const loadProducts = async () => {
    const catalog = await fetchProductCatalog();
    setProducts(catalog);
  };

  const isManager = role?.split(',').includes('ROLE_MANAGER') || false;

  const fetchCustomerProfile = async (accountEmail) => {
    const customer = await apiRequest('/register-customer/me');
    return makeClientProfile(customer, accountEmail);
  };

  const rememberCustomerProfile = (nextProfile) => {
    localStorage.setItem(PROFILE_KEY, JSON.stringify(nextProfile));
    setProfile(nextProfile);
  };

  useEffect(() => {
    let cancelled = false;
    const restore = async () => {
      if (!email) {
        setLoadingProducts(false);
        return;
      }
      try {
        const currentUser = await apiRequest('/auth/me');
        if (!cancelled) {
          setRole(currentUser.role);
          setAuthenticated(true);
          setAuthOpen(false);
        }
        await loadProducts();
        if (!cancelled) {
          let restoredProfile = null;
          try {
            restoredProfile = await fetchCustomerProfile(email);
            if (!cancelled) rememberCustomerProfile(restoredProfile);
          } catch (profileError) {
            if (!cancelled && profileError.status === 404) {
              localStorage.removeItem(PROFILE_KEY);
              setProfile(null);
              setProfileOpen(true);
            } else if (!cancelled && profileError.status !== 401) {
              showToast(profileError.message);
            }
          }
          if (!cancelled && restoredProfile) {
            const params = new URLSearchParams({ customerId: restoredProfile.customerId });
            try {
              const cart = await apiRequest(`/shopping/viewMyCart?${params}`);
              if (!cancelled) setCartItems(Array.isArray(cart?.cartItems) ? cart.cartItems : []);
            } catch (cartError) {
              if (!cancelled && cartError.status === 404) setCartItems([]);
            }
          }
        }
      } catch (error) {
        if (!cancelled && error.status !== 401) showToast(error.message);
        if (!cancelled && error.status === 401) {
          setAuthenticated(false);
          setRole(null);
          setProducts([]);
        }
        if (!cancelled) setAuthOpen(true);
      } finally {
        if (!cancelled) setLoadingProducts(false);
      }
    };
    restore();
    return () => { cancelled = true; };
  }, []);

  const visibleProducts = useMemo(() => {
    const query = search.trim().toLowerCase();
    const filtered = products.filter((product) => {
      const matchesCategory = activeCategory === 'all' || productCategory(product) === activeCategory;
      const matchesQuery = !query || `${product.productName || ''} ${product.productDescription || ''} ${product.merchantDetail || ''}`.toLowerCase().includes(query);
      return matchesCategory && matchesQuery;
    });
    if (sort === 'price-low') filtered.sort((a, b) => Number(a.productPrice) - Number(b.productPrice));
    if (sort === 'price-high') filtered.sort((a, b) => Number(b.productPrice) - Number(a.productPrice));
    if (sort === 'name') filtered.sort((a, b) => (a.productName || '').localeCompare(b.productName || ''));
    return filtered;
  }, [products, search, activeCategory, sort]);

  const totalPages = Math.ceil(visibleProducts.length / PRODUCT_PAGE_SIZE);
  const currentPageNo = Math.min(pageNo, Math.max(0, totalPages - 1));
  const pageProducts = visibleProducts.slice(
    currentPageNo * PRODUCT_PAGE_SIZE,
    (currentPageNo + 1) * PRODUCT_PAGE_SIZE
  );

  const cartCount = cartItems.reduce((sum, item) => sum + Number(item.quantity || 0), 0);
  const cartSubtotal = cartItems.reduce((sum, item) => sum + Number(item.priceAtCheckout || 0), 0);

  const handleAuth = async (credentials, mode) => {
    if (mode === 'register') {
      await apiRequest('/auth/register', { method: 'POST', body: JSON.stringify(credentials) });
    }
    await apiRequest('/auth/login', { method: 'POST', body: JSON.stringify(credentials) });
    const currentUser = await apiRequest('/auth/me');
    setRole(currentUser.role);
    setAuthenticated(true);
    const profileMatchesAccount = profile && (!profile.email || profile.email.toLowerCase() === credentials.email.toLowerCase());
    if (!profileMatchesAccount) {
      localStorage.removeItem(PROFILE_KEY);
      setProfile(null);
    }
    localStorage.setItem(EMAIL_KEY, credentials.email);
    setEmail(credentials.email);
    await loadProducts();
    setAuthOpen(false);
    try {
      const savedProfile = await fetchCustomerProfile(credentials.email);
      rememberCustomerProfile(savedProfile);
      setProfileOpen(false);
      showToast('Welcome back.');
    } catch (error) {
      if (error.status === 404) {
        localStorage.removeItem(PROFILE_KEY);
        setProfile(null);
        setProfileOpen(true);
        showToast('Add your delivery details to your account to start shopping.');
      } else {
        showToast(error.message);
      }
    }
  };

  const saveProfile = async (form) => {
    const saved = await apiRequest('/register-customer', {
      method: 'POST',
      body: JSON.stringify(form),
    });
    const nextProfile = makeClientProfile(saved, email);
    rememberCustomerProfile(nextProfile);
    setProfileOpen(false);
    showToast('Delivery profile saved. You’re ready to shop.');
  };

  const signOut = async () => {
    try {
      await apiRequest('/auth/logout', { method: 'POST' });
    } catch {
      // Clear the local view even if the server session already expired.
    }
    localStorage.removeItem(EMAIL_KEY);
    localStorage.removeItem(PROFILE_KEY);
    setAuthenticated(false);
    setRole(null);
    setEmail('');
    setProfile(null);
    setProducts([]);
    setCartItems([]);
    setCartOpen(false);
    setAuthMode('login');
    setAuthOpen(true);
  };

  const ensureProfile = () => {
    if (!authenticated) {
      setAuthMode('login');
      setAuthOpen(true);
      return false;
    }
    if (!profile?.customerId || (profile.email && profile.email.toLowerCase() !== email.toLowerCase())) {
      setProfileOpen(true);
      return false;
    }
    return true;
  };

  const openOrders = async () => {
    if (!ensureProfile()) return;
    setOrdersOpen(true);
    setLoadingOrders(true);
    try {
      const data = await apiRequest('/checkout/my-orders');
      setOrders(Array.isArray(data) ? data : []);
    } catch (error) {
      setOrders([]);
      showToast(error.message);
      if (error.status === 401) {
        setAuthenticated(false);
        setAuthOpen(true);
        setOrdersOpen(false);
      }
    } finally {
      setLoadingOrders(false);
    }
  };

  const saveManagerProduct = async (product) => {
    const updating = Boolean(editingProduct);
    const payload = updating ? { ...product, productId: editingProduct.productId } : product;
    await apiRequest(updating ? '/register-product/update' : '/register-product', {
      method: updating ? 'PATCH' : 'POST',
      body: JSON.stringify(payload),
    });
    setEditingProduct(null);
    await loadProducts();
    setPageNo(0);
    showToast(updating ? 'Product updated.' : 'Product added to the store.');
  };

  const deleteProduct = async (product) => {
    if (!window.confirm(`Delete ${product.productName}?`)) return;
    try {
      await apiRequest(`/register-product/${product.productId}`, { method: 'DELETE' });
      setProducts((items) => items.filter((item) => Number(item.productId) !== Number(product.productId)));
      if (Number(editingProduct?.productId) === Number(product.productId)) setEditingProduct(null);
      setPageNo(0);
      showToast('Product deleted.');
    } catch (error) {
      showToast(error.message);
    }
  };

  const addToCart = async (product) => {
    if (!ensureProfile()) return;
    try {
      const params = new URLSearchParams({
        customerId: profile.customerId,
        productId: product.productId,
        quantity: '1',
      });
      const data = await apiRequest(`/shopping/addToCart?${params}`, { method: 'POST' });
      setCartItems(Array.isArray(data?.cartItems) ? data.cartItems : []);
      showToast(`${product.productName} added to your cart.`);
    } catch (error) {
      showToast(error.message);
      if (error.status === 401) {
        setAuthenticated(false);
        setAuthOpen(true);
      }
    }
  };

  const openCart = async () => {
    if (!ensureProfile()) return;
    try {
      const params = new URLSearchParams({ customerId: profile.customerId });
      const data = await apiRequest(`/shopping/viewMyCart?${params}`);
      setCartItems(Array.isArray(data?.cartItems) ? data.cartItems : []);
    } catch (error) {
      if (error.status === 404) setCartItems([]);
      else {
        showToast(error.message);
        if (error.status === 401) {
          setAuthenticated(false);
          setAuthOpen(true);
          return;
        }
      }
    }
    setCartOpen(true);
  };

  const removeFromCart = async (productId) => {
    try {
      const params = new URLSearchParams({ customerId: profile.customerId, productId });
      await apiRequest(`/shopping/removeFromCart?${params}`, { method: 'DELETE' });
      setCartItems((items) => items.filter((item) => Number(item.product?.productId) !== Number(productId)));
      showToast('Item removed from your cart.');
    } catch (error) {
      showToast(error.message);
      if (error.status === 401) {
        setAuthenticated(false);
        setAuthOpen(true);
      }
    }
  };

  const checkout = async () => {
    if (!profile?.customerId || cartItems.length === 0) return;
    try {
      const params = new URLSearchParams({ customerId: profile.customerId, paymentMethod });
      const order = await apiRequest(`/checkout?${params}`, { method: 'POST' });
      setCartItems([]);
      setCartOpen(false);
      setCheckoutOrder(order);
      setProducts((items) => items.map((product) => {
        const bought = (order.orderItems || []).find((item) => Number(item.product?.productId) === Number(product.productId));
        return bought ? { ...product, availableQuantity: Math.max(0, Number(product.availableQuantity) - Number(bought.quantity)) } : product;
      }));
    } catch (error) {
      showToast(error.message);
      if (error.status === 401) {
        setAuthenticated(false);
        setAuthOpen(true);
      }
    }
  };

  return (
    <div className="app-shell">
      <header className="site-header">
        <div className="header-main">
          <a className="brand" href="#top" aria-label="Shopnest home">
            <span className="brand-word">shopnest</span><span className="brand-country">.in</span>
            <span className="brand-smile" />
          </a>
          <div className="deliver-to">
            <MapPin size={17} />
            <span><small>Deliver to</small><strong>{profile?.fullName || 'your location'}</strong></span>
          </div>
          <form className="search-bar" onSubmit={(event) => event.preventDefault()}>
            <label className="search-department" htmlFor="search-input">All <ChevronDown size={14} /></label>
            <input id="search-input" value={search} onChange={(event) => { setSearch(event.target.value); setPageNo(0); }} placeholder="Search Shopnest" />
            <button type="submit" aria-label="Search"><Search size={21} /></button>
          </form>
          <div className="header-account">
            <button className="header-link account-link" onClick={() => authenticated ? showToast(`Signed in as ${email}`) : setAuthOpen(true)}>
              <small>{authenticated ? (isManager ? 'Manager account' : 'Hello') : 'Welcome'}</small>
              <strong>{authenticated ? (email.split('@')[0] || 'Account') : 'Sign in'}</strong>
            </button>
            {authenticated && <button className="signout-link" onClick={signOut}>Sign out</button>}
          </div>
          <button className="header-link returns-link" onClick={openOrders}>
            <small>Returns</small><strong>& Orders</strong>
          </button>
          <button className="cart-link" onClick={openCart} aria-label={`Open cart, ${cartCount} items`}>
            <span className="cart-icon-wrap"><ShoppingCart size={30} /><b>{cartCount}</b></span>
            <strong>Cart</strong>
          </button>
        </div>
        <nav className="department-nav" aria-label="Departments">
          <button className="nav-menu" onClick={() => { setActiveCategory('all'); setPageNo(0); }}><Menu size={17} /> All</button>
          <button onClick={() => { setActiveCategory('tech'); setPageNo(0); }}>Electronics</button>
          <button onClick={() => { setActiveCategory('home'); setPageNo(0); }}>Home & Kitchen</button>
          <button onClick={() => { setActiveCategory('fashion'); setPageNo(0); }}>Fashion</button>
          <button onClick={() => { setActiveCategory('books'); setPageNo(0); }}>Books</button>
          <button onClick={() => { setActiveCategory('beauty'); setPageNo(0); }}>Beauty</button>
          <span className="nav-promo"><Sparkles size={15} /> Everyday finds, delivered with care</span>
        </nav>
      </header>

      <main id="top">
        <section className="hero-wrap">
          <div className="hero-copy">
            <div className="eyebrow"><Sparkles size={15} /> A little something for every day</div>
            <h1>Good finds.<br /><em>Great everyday.</em></h1>
            <p>Discover useful favorites, thoughtful upgrades, and the little things that make home feel more like yours.</p>
            <button className="hero-cta" onClick={() => document.getElementById('catalog')?.scrollIntoView({ behavior: 'smooth' })}>
              Explore the store <ArrowRight size={18} />
            </button>
        <div className="hero-proof"><span><Star size={14} fill="currentColor" /> Everyday essentials</span><span>Easy, secure checkout</span></div>
          </div>
          <div className="hero-art" aria-hidden="true">
            <div className="hero-sun" />
            <div className="hero-orbit orbit-one" />
            <div className="hero-orbit orbit-two" />
            <div className="hero-product-card hero-card-one"><Headphones size={43} /><span>Daily favorites</span></div>
            <div className="hero-product-card hero-card-two"><Smartphone size={46} /><span>Smart choices</span></div>
            <div className="hero-product-card hero-card-three"><Home size={41} /><span>Made for home</span></div>
            <div className="hero-sticker"><strong>Find</strong><span>your next<br />favorite</span></div>
          </div>
          <div className="hero-bottom-line"><span>Thoughtful picks</span><span>Simple shopping</span><span>Everyday value</span></div>
        </section>

        {isManager && (
          <ManagerDashboard
            editingProduct={editingProduct}
            onSave={saveManagerProduct}
            onCancelEdit={() => setEditingProduct(null)}
          />
        )}

        <section className="benefit-strip" aria-label="Shopping benefits">
          <div><Truck size={22} /><span><strong>Delivery details at checkout</strong><small>Add your address to your profile</small></span></div>
          <div><PackageCheck size={22} /><span><strong>Made for easy browsing</strong><small>Find your next everyday favorite</small></span></div>
          <div><Check size={22} /><span><strong>Checkout in a few steps</strong><small>Your cart stays yours</small></span></div>
        </section>

        <section className="catalog-section" id="catalog">
          <div className="catalog-heading">
            <div>
              <div className="section-kicker">The Shopnest edit</div>
              <h2>Find something you’ll love</h2>
              <p>{authenticated ? `${visibleProducts.length} products picked for everyday living` : 'Sign in to browse products and see live availability'}</p>
            </div>
            <label className="sort-control">Sort by
              <select value={sort} onChange={(event) => { setSort(event.target.value); setPageNo(0); }}>
                <option value="featured">Featured</option>
                <option value="price-low">Price: low to high</option>
                <option value="price-high">Price: high to low</option>
                <option value="name">Name</option>
              </select>
            </label>
          </div>

          <div className="category-pills" role="tablist" aria-label="Filter products by department">
            {categories.map(({ id, label, Icon }) => (
              <button key={id} className={activeCategory === id ? 'category-pill active' : 'category-pill'} onClick={() => { setActiveCategory(id); setPageNo(0); }} role="tab" aria-selected={activeCategory === id}>
                <Icon size={16} /> {label}
              </button>
            ))}
          </div>

          {loadingProducts ? (
            <div className="loading-state"><span className="spinner" /> Loading the good stuff…</div>
          ) : !authenticated ? (
            <div className="empty-catalog">
              <div className="empty-icon"><UserRound size={26} /></div>
              <h3>Your next great find is one sign-in away</h3>
              <p>Sign in to see products, availability, and your shopping cart.</p>
              <button className="primary-button" onClick={() => setAuthOpen(true)}>Sign in to shop</button>
            </div>
          ) : visibleProducts.length === 0 ? (
            <div className="empty-catalog">
              <div className="empty-icon"><Tag size={26} /></div>
              <h3>{products.length ? 'No matches this time' : 'Your store is ready for its first product'}</h3>
              <p>{products.length ? 'Try another search or department.' : isManager ? 'Add the first listing from your manager workspace above.' : 'Products added by the store manager will show up here.'}</p>
              {products.length > 0 && <button className="text-button" onClick={() => { setSearch(''); setActiveCategory('all'); setPageNo(0); }}>Clear filters</button>}
            </div>
          ) : (
            <div className="product-grid">
              {pageProducts.map((product, index) => (
                <ProductCard
                  key={product.productId}
                  product={product}
                  index={index}
                  onAdd={addToCart}
                  authenticated={authenticated}
                  isManager={isManager}
                  onEdit={() => setEditingProduct(product)}
                  onDelete={() => deleteProduct(product)}
                />
              ))}
            </div>
          )}
          {authenticated && !loadingProducts && totalPages > 1 && (
            <nav className="pagination-controls" aria-label="Product pages">
              <button className="secondary-button" disabled={currentPageNo === 0} onClick={() => setPageNo(currentPageNo - 1)}>Previous</button>
              <label>Page no
                <input
                  type="number"
                  min="1"
                  max={totalPages}
                  value={currentPageNo + 1}
                  onChange={(event) => {
                    const requestedPage = Number(event.target.value);
                    if (Number.isInteger(requestedPage) && requestedPage >= 1 && requestedPage <= totalPages) {
                      setPageNo(requestedPage - 1);
                    }
                  }}
                />
                <span>of {totalPages}</span>
              </label>
              <span className="page-size-note">{PRODUCT_PAGE_SIZE} per page</span>
              <button className="secondary-button" disabled={currentPageNo >= totalPages - 1} onClick={() => setPageNo(currentPageNo + 1)}>Next</button>
            </nav>
          )}
        </section>
      </main>

      <footer className="site-footer">
        <button onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}>Back to top ↑</button>
        <div className="footer-bottom"><a className="brand footer-brand" href="#top"><span className="brand-word">shopnest</span><span className="brand-country">.in</span><span className="brand-smile" /></a><span>Good finds for everyday living.</span><span>© 2026 Shopnest</span></div>
      </footer>

      <AuthModal
        open={authOpen}
        mode={authMode}
        onMode={setAuthMode}
        onClose={() => setAuthOpen(false)}
        onSubmit={handleAuth}
      />
      <ProfileModal open={profileOpen} onClose={() => setProfileOpen(false)} onSubmit={saveProfile} />
      <CartDrawer
        open={cartOpen}
        items={cartItems}
        subtotal={cartSubtotal}
        paymentMethod={paymentMethod}
        onPaymentMethod={setPaymentMethod}
        onClose={() => setCartOpen(false)}
        onRemove={removeFromCart}
        onCheckout={checkout}
      />
      {checkoutOrder && (
        <CheckoutSuccess
          order={checkoutOrder}
          onClose={() => setCheckoutOrder(null)}
          onViewOrders={() => { setCheckoutOrder(null); openOrders(); }}
        />
      )}
      <OrdersModal open={ordersOpen} orders={orders} loading={loadingOrders} onClose={() => setOrdersOpen(false)} />
      {toast && <div className="toast" role="status"><Check size={17} /> {toast}</div>}
    </div>
  );
}

function ManagerDashboard({ editingProduct, onSave, onCancelEdit }) {
  const [form, setForm] = useState(createEmptyManagerProduct);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setForm(editingProduct ? {
      productName: editingProduct.productName || '',
      productPrice: editingProduct.productPrice || '',
      availableQuantity: editingProduct.availableQuantity ?? 0,
      productDescription: editingProduct.productDescription || '',
      merchantDetail: editingProduct.merchantDetail || 'Shopnest Marketplace',
    } : createEmptyManagerProduct());
    setError('');
  }, [editingProduct]);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await onSave({
        ...form,
        productPrice: Number(form.productPrice),
        availableQuantity: Number(form.availableQuantity),
      });
      if (!editingProduct) {
        setForm(createEmptyManagerProduct());
      }
    } catch (saveError) {
      setError(saveError.message || 'Could not save this product.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="manager-panel" id="manager-tools">
      <div className="manager-panel-heading">
        <div><span className="section-kicker">Manager workspace</span><h2>Manage store products</h2></div>
        <p>Add products to the store or update existing listings.</p>
      </div>
      {error && <div className="form-error" role="alert">{error}</div>}
      <form className="manager-product-form" onSubmit={submit}>
        <label>Product name<input required value={form.productName} onChange={(event) => setForm({ ...form, productName: event.target.value })} /></label>
        <label>Price (₹)<input type="number" min="0.01" step="0.01" required value={form.productPrice} onChange={(event) => setForm({ ...form, productPrice: event.target.value })} /></label>
        <label>Stock quantity<input type="number" min="0" step="1" required value={form.availableQuantity} onChange={(event) => setForm({ ...form, availableQuantity: event.target.value })} /></label>
        <label className="manager-description">Description<textarea required rows="2" value={form.productDescription} onChange={(event) => setForm({ ...form, productDescription: event.target.value })} /></label>
        <label>Seller<input value={form.merchantDetail} onChange={(event) => setForm({ ...form, merchantDetail: event.target.value })} /></label>
        <div className="manager-form-actions">
          <button className="primary-button" disabled={saving}>{saving ? 'Saving…' : editingProduct ? 'Save changes' : 'Add product'}</button>
          {editingProduct && <button type="button" className="text-button" onClick={onCancelEdit}>Cancel edit</button>}
        </div>
      </form>
    </section>
  );
}

function ProductCard({ product, index, onAdd, authenticated, isManager, onEdit, onDelete }) {
  const category = productCategory(product);
  const stock = Number(product.availableQuantity || 0);
  const iconMap = {
    tech: Smartphone,
    home: Home,
    fashion: Shirt,
    books: BookOpen,
    beauty: Flower2,
    everyday: Sparkles,
  };
  const Icon = iconMap[category] || Sparkles;
  const categoryLabel = categories.find((item) => item.id === category)?.label || 'Everyday finds';

  return (
    <article className="product-card" style={{ '--card-index': index }}>
      <div className={`product-art art-${category}`}>
        <div className="art-glow" />
        <Icon className="product-art-icon" size={58} strokeWidth={1.4} />
        <span className="art-label">{categoryLabel}</span>
        {index % 3 === 0 && <span className="art-chip"><Sparkles size={12} /> Shop the edit</span>}
      </div>
      <div className="product-info">
        <div className="product-category">{categoryLabel}</div>
        <h3 title={product.productName}>{product.productName || 'Everyday essential'}</h3>
        <p className="product-description">{product.productDescription || 'A useful favorite, selected for everyday living.'}</p>
        <div className="price-stock-row">
          <strong className="product-price">{formatPrice(product.productPrice)}</strong>
          <span className={stock > 0 ? 'stock-note' : 'stock-note out'}>{stock > 0 ? `${stock} in stock` : 'Out of stock'}</span>
        </div>
        <button className="add-button" disabled={!authenticated || stock <= 0} onClick={() => onAdd(product)}>
          <Plus size={17} /> {stock > 0 ? 'Add to cart' : 'Out of stock'}
        </button>
        {isManager && (
          <div className="manager-product-actions">
            <button onClick={onEdit}><Pencil size={14} /> Edit</button>
            <button onClick={onDelete}><Trash2 size={14} /> Delete</button>
          </div>
        )}
      </div>
    </article>
  );
}

function AuthModal({ open, mode, onMode, onClose, onSubmit }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  if (!open) return null;

  const submit = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      await onSubmit({ email: email.trim(), password }, mode);
    } catch (requestError) {
      setError(requestError.message || 'We couldn’t sign you in. Please try again.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <ModalFrame onClose={onClose} className="auth-modal">
      <div className="modal-brand"><span className="brand-word">shopnest</span><span className="brand-country">.in</span></div>
      <div className="modal-intro">
        <span className="modal-icon"><UserRound size={19} /></span>
        <div><span className="section-kicker">Your Shopnest account</span><h2>{mode === 'login' ? 'Welcome back' : 'Create your account'}</h2></div>
      </div>
      <p className="modal-copy">{mode === 'login' ? 'Sign in to discover your next everyday favorite.' : 'Join us for a simpler way to shop the things you love.'}</p>
      {error && <div className="form-error" role="alert">{error}</div>}
      <form className="modal-form" onSubmit={submit}>
        <label>Email address<input type="email" autoComplete="email" required value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" /></label>
        <label>Password<input type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} minLength="6" required value={password} onChange={(event) => setPassword(event.target.value)} placeholder="At least 6 characters" /></label>
        <button className="primary-button full-button" disabled={busy}>{busy ? 'Please wait…' : mode === 'login' ? 'Sign in securely' : 'Create account'}</button>
      </form>
      <div className="modal-switch">
        {mode === 'login' ? 'New to Shopnest?' : 'Already have an account?'}
        <button onClick={() => { setError(''); onMode(mode === 'login' ? 'register' : 'login'); }}>{mode === 'login' ? 'Create an account' : 'Sign in'}</button>
      </div>
      <div className="secure-note"><Check size={14} /> Your account is secured by the store</div>
    </ModalFrame>
  );
}

function ProfileModal({ open, onClose, onSubmit }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  if (!open) return null;
  const submit = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError('');
    const formData = new FormData(event.currentTarget);
    const customer = {
      firstName: formData.get('firstName'),
      lastName: formData.get('lastName'),
      phoneNumber: formData.get('phoneNumber'),
      address: {
        houseNo: formData.get('houseNo'),
        street: formData.get('street'),
        city: formData.get('city'),
        state: formData.get('state'),
        zipCode: formData.get('zipCode'),
        country: formData.get('country'),
      },
    };
    try {
      await onSubmit(customer);
    } catch (requestError) {
      setError(requestError.message || 'We couldn’t save your profile.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <ModalFrame onClose={onClose} className="profile-modal">
      <div className="modal-topline"><span className="section-kicker">One quick step</span><span className="step-count">Delivery profile</span></div>
      <h2>Where should we send your finds?</h2>
      <p className="modal-copy">Add your delivery details once to start using your cart and checkout.</p>
      {error && <div className="form-error" role="alert">{error}</div>}
      <form className="modal-form profile-form" onSubmit={submit}>
        <div className="form-two"><label>First name<input name="firstName" autoComplete="given-name" required /></label><label>Last name<input name="lastName" autoComplete="family-name" required /></label></div>
        <label>Phone number<input name="phoneNumber" type="tel" autoComplete="tel" required /></label>
        <div className="form-two"><label>House / flat<input name="houseNo" autoComplete="address-line1" required /></label><label>Street<input name="street" autoComplete="address-line2" required /></label></div>
        <div className="form-two"><label>City<input name="city" autoComplete="address-level2" required /></label><label>State<input name="state" autoComplete="address-level1" required /></label></div>
        <div className="form-two"><label>Postal code<input name="zipCode" autoComplete="postal-code" required /></label><label>Country<input name="country" autoComplete="country-name" defaultValue="India" required /></label></div>
        <button className="primary-button full-button" disabled={busy}>{busy ? 'Saving…' : 'Save delivery profile'}</button>
      </form>
    </ModalFrame>
  );
}

function CartDrawer({ open, items, subtotal, paymentMethod, onPaymentMethod, onClose, onRemove, onCheckout }) {
  if (!open) return null;
  const count = items.reduce((sum, item) => sum + Number(item.quantity || 0), 0);
  return (
    <div className="drawer-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}>
      <aside className="cart-drawer" aria-label="Shopping cart">
        <div className="drawer-heading"><div><span className="section-kicker">Your bag</span><h2>Shopping cart <small>({count})</small></h2></div><button className="icon-button" onClick={onClose} aria-label="Close cart"><X size={21} /></button></div>
        {items.length === 0 ? (
          <div className="cart-empty"><div className="empty-icon"><ShoppingCart size={25} /></div><h3>Your cart is waiting</h3><p>Add a few everyday favorites and they’ll show up here.</p><button className="secondary-button" onClick={onClose}>Continue shopping</button></div>
        ) : (
          <>
            <div className="cart-items">
              {items.map((item) => (
                <div className="cart-item" key={item.cartItemId || item.product?.productId}>
                  <div className={`cart-item-art art-${productCategory(item.product || {})}`}><ShoppingCart size={22} /></div>
                  <div className="cart-item-main"><strong>{item.product?.productName || 'Product'}</strong><span>Qty {item.quantity}</span><small>{formatPrice(item.priceAtCheckout)} total</small></div>
                  <button className="remove-button" onClick={() => onRemove(item.product?.productId)} aria-label={`Remove ${item.product?.productName || 'item'}`}><Trash2 size={17} /></button>
                </div>
              ))}
            </div>
            <div className="cart-summary">
              <label className="payment-choice" htmlFor="payment-method">
                <span>Payment method <small>Demo payment only; no real charge</small></span>
                <select id="payment-method" value={paymentMethod} onChange={(event) => onPaymentMethod(event.target.value)}>
                  <option value="SIMULATED_UPI">UPI simulation</option>
                  <option value="SIMULATED_CARD">Card simulation</option>
                </select>
              </label>
              <div><span>Subtotal</span><strong>{formatPrice(subtotal)}</strong></div>
              <small>Shipping and final details are confirmed at checkout.</small>
              <button className="primary-button full-button checkout-button" onClick={onCheckout}>Pay & place order <ArrowRight size={17} /></button>
            </div>
          </>
        )}
      </aside>
    </div>
  );
}

function CheckoutSuccess({ order, onClose, onViewOrders }) {
  return (
    <ModalFrame onClose={onClose} className="success-modal">
      <div className="success-mark"><Check size={28} /></div>
      <span className="section-kicker">Order placed</span>
      <h2>Thank you for shopping with us.</h2>
      <p className="modal-copy">Your simulated payment succeeded. No real charge was made, and your order is saved.</p>
      <div className="order-details">
        <div><span>Order number</span><strong>#{order.orderId}</strong></div>
        <div><span>Transaction</span><strong className="transaction-id">{order.transactionId}</strong></div>
        <div><span>Items</span><strong>{order.orderItems?.length || 0}</strong></div>
        <div><span>Total</span><strong>{formatPrice(order.totalAmount)}</strong></div>
        <div><span>Payment</span><strong>{order.paymentMethod}</strong></div>
        <div><span>Payment status</span><strong className="status-success">{order.status || 'SUCCESS'}</strong></div>
      </div>
      <div className="success-actions">
        <button className="primary-button full-button" onClick={onViewOrders}>View my orders</button>
        <button className="text-button" onClick={onClose}>Continue shopping</button>
      </div>
    </ModalFrame>
  );
}

function OrdersModal({ open, orders, loading, onClose }) {
  if (!open) return null;
  return (
    <ModalFrame onClose={onClose} className="orders-modal">
      <div className="orders-heading">
        <span className="section-kicker">Your Shopnest account</span>
        <h2>Your orders</h2>
        <p className="modal-copy">Order and simulated payment details are saved here.</p>
      </div>
      {loading ? (
        <div className="loading-state"><span className="spinner" /> Loading your orders…</div>
      ) : orders.length === 0 ? (
        <div className="orders-empty"><PackageCheck size={28} /><h3>No orders yet</h3><p>Your completed orders will show here.</p></div>
      ) : (
        <div className="orders-list">
          {orders.map((order) => (
            <article className="order-card" key={order.orderId}>
              <div className="order-card-heading">
                <div><span>Order #{order.orderId}</span><small>{order.date} · {order.time?.slice(0, 5)}</small></div>
                <strong className={order.status === 'SUCCESS' ? 'status-success' : 'status-other'}>{order.status || 'UNKNOWN'}</strong>
              </div>
              <div className="order-card-items">
                {(order.items || []).map((item) => (
                  <div key={item.productId}>
                    <span>{item.productName} <small>× {item.quantity}</small></span>
                    <strong>{formatPrice(item.lineTotal)}</strong>
                  </div>
                ))}
              </div>
              <div className="order-card-footer">
                <span>{order.paymentMethod || 'Payment details unavailable'}{order.transactionId ? ` · ${order.transactionId}` : ''}</span>
                <strong>{formatPrice(order.totalAmount)}</strong>
              </div>
            </article>
          ))}
        </div>
      )}
    </ModalFrame>
  );
}

function ModalFrame({ children, onClose, className = '' }) {
  return (
    <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}>
      <section className={`modal-card ${className}`} role="dialog" aria-modal="true">
        <button className="modal-close icon-button" onClick={onClose} aria-label="Close dialog"><X size={20} /></button>
        {children}
      </section>
    </div>
  );
}

export default App;
