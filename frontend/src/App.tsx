import { useEffect, useMemo, useRef, useState } from 'react'
import { api } from './api'

type IconName = 'bag' | 'user' | 'arrow' | 'arrowRight' | 'close' | 'menu' | 'search' | 'heart' | 'plus' | 'minus' | 'check' | 'truck' | 'sparkle' | 'shield' | 'instagram' | 'logout' | 'chart' | 'box' | 'receipt' | 'settings' | 'chevron' | 'trash' | 'eye'
type Product = { id: string; name: string; category: string; description: string; price: number; image: string; badge?: string; active: boolean; stock: number }
type CartItem = { id: string; quantity: number }
type User = { name: string; email: string; password: string; role: 'admin' | 'customer' }
type Order = { id: string; customer: string; email: string; items: { name: string; quantity: number; price: number }[]; total: number; date: string; status: 'Aguardando confirmação' | 'Confirmado' | 'Em rota' | 'Entregue' }

const image = (id: string, width = 900) => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=${width}&q=85`

const initialProducts: Product[] = [
  { id: 'demo-1', name: 'Bolo de Chocolate Belga', category: 'Bolos', description: 'Camadas generosas de chocolate belga e ganache cremosa.', price: 89.9, image: image('photo-1684436060012-db6da717e7c7'), badge: 'Mais pedido', active: true, stock: 20 },
  { id: 'demo-2', name: 'Tartelete de Morango', category: 'Tortas', description: 'Massa amanteigada, creme de baunilha e morangos frescos.', price: 28.9, image: image('photo-1503485838016-53579610c389'), badge: 'Favorito', active: true, stock: 20 },
  { id: 'demo-3', name: 'Brownie Intenso', category: 'Docinhos', description: 'Casquinha delicada e um coração de chocolate irresistível.', price: 19.9, image: image('photo-1606313564200-e75d5e30476c'), active: true, stock: 20 },
  { id: 'demo-4', name: 'Macarons da Casa', category: 'Docinhos', description: 'Uma seleção de sabores para colorir qualquer momento.', price: 42.9, image: image('photo-1528975604071-b4dc52a2d18c'), badge: 'Caixa com 6', active: true, stock: 20 },
  { id: 'demo-5', name: 'Cookie de Chocolate', category: 'Docinhos', description: 'Crocante por fora, macio por dentro e cheio de chocolate.', price: 16.9, image: image('photo-1654922208028-0b106de62af1'), active: true, stock: 20 },
  { id: 'demo-6', name: 'Fatia de Chocolate', category: 'Bolos', description: 'Uma fatia especial para transformar a pausa do dia.', price: 32.9, image: image('photo-1637944220604-c5f28faac604'), active: true, stock: 20 },
]

const money = (value: number) => value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
// 'Confirmado' e 'Em rota' usam o mesmo acento visual (amarelo "em andamento");
// só o primeiro e o último passo têm cor própria.
const statusClass = (status: Order['status']) => status === 'Aguardando confirmação' ? 'new' : status === 'Entregue' ? 'done' : 'progress'
const getStored = <T,>(key: string, fallback: T): T => {
  try { const value = localStorage.getItem(key); return value ? JSON.parse(value) as T : fallback } catch { return fallback }
}

function Icon({ name, size = 20, strokeWidth = 1.8 }: { name: IconName; size?: number; strokeWidth?: number }) {
  const paths: Record<IconName, React.ReactNode> = {
    bag: <><path d="M4 8h16l-1 13H5L4 8Z"/><path d="M9 9V6a3 3 0 0 1 6 0v3"/></>,
    user: <><circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/></>,
    arrow: <><path d="M5 12h14"/><path d="m13 6 6 6-6 6"/></>,
    arrowRight: <><path d="M5 12h14"/><path d="m13 6 6 6-6 6"/></>,
    close: <><path d="M5 5 19 19M19 5 5 19"/></>,
    menu: <><path d="M4 7h16M4 12h16M4 17h16"/></>,
    search: <><circle cx="10.8" cy="10.8" r="6.8"/><path d="m16 16 5 5"/></>,
    heart: <path d="M20.8 8.6c0 4.1-8.8 10.1-8.8 10.1S3.2 12.7 3.2 8.6a4.6 4.6 0 0 1 8.8-1.8 4.6 4.6 0 0 1 8.8 1.8Z"/>,
    plus: <path d="M12 5v14M5 12h14"/>,
    minus: <path d="M5 12h14"/>,
    check: <path d="m4 12 5 5L20 6"/>,
    truck: <><path d="M2 6h12v11H2zM14 10h4l4 4v3h-8z"/><circle cx="6" cy="18" r="2"/><circle cx="18" cy="18" r="2"/></>,
    sparkle: <><path d="m12 2 1.7 7.3L21 11l-7.3 1.7L12 20l-1.7-7.3L3 11l7.3-1.7L12 2ZM19 19l.5 2 .5-2 2-.5-2-.5-.5-2-.5 2-2 .5 2 .5Z"/></>,
    shield: <><path d="M12 2 4 5v6c0 5 3.5 8.5 8 11 4.5-2.5 8-6 8-11V5l-8-3Z"/><path d="m9 12 2 2 4-4"/></>,
    instagram: <><rect x="3" y="3" width="18" height="18" rx="5"/><circle cx="12" cy="12" r="4"/><circle cx="17.5" cy="6.5" r=".7" fill="currentColor" stroke="none"/></>,
    logout: <><path d="M10 4H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h5M14 7l5 5-5 5M8 12h11"/></>,
    chart: <><path d="M4 20V10M10 20V4M16 20v-7M22 20v-11"/></>,
    box: <><path d="m12 2 9 5-9 5-9-5 9-5ZM3 7v10l9 5 9-5V7M12 12v10"/></>,
    receipt: <><path d="M5 2h14v20l-3-2-4 2-4-2-3 2V2ZM9 8h6M9 12h6M9 16h4"/></>,
    settings: <><circle cx="12" cy="12" r="3"/><path d="M10 2h4l.5 2.2 2 .9 2-.9 2.8 2.8-.9 2 .9 2L23 12l-1.7 1.1-.9 2 .9 2-2.8 2.8-2-.9-2 .9L14 22h-4l-.5-2.2-2-.9-2 .9-2.8-2.8.9-2-.9-2L1 12l1.7-1.1.9-2-.9-2L5.5 4l2 .9 2-.9L10 2Z"/></>,
    chevron: <path d="m6 9 6 6 6-6"/>,
    trash: <><path d="M4 7h16M9 7V4h6v3M6 7l1 14h10l1-14M10 11v6M14 11v6"/></>,
    eye: <><path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></>,
  }
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name]}</svg>
}

function Brand({ light = false }: { light?: boolean }) {
  return <div className={`brand ${light ? 'brand-light' : ''}`}><span className="brand-mark"><span>✳</span></span><span className="brand-type">doce<span>atelier</span><small>CONFEITARIA ARTESANAL</small></span></div>
}

// Animate existing storefront elements without rerendering React on every scroll.
function useStorefrontMotion(view: string, catalogKey: string) {
  useEffect(() => {
    if (view !== 'shop') return
    const shell = document.querySelector<HTMLElement>('.site-shell')
    if (!shell) return
    const preference = window.matchMedia('(prefers-reduced-motion: reduce)')
    let dispose = () => {}

    const setup = () => {
      dispose()
      if (preference.matches) return
      const animations = new Set<Animation>()
      const animate = (element: Element, frames: Keyframe[], options: KeyframeAnimationOptions) => {
        const animation = element.animate(frames, options)
        animations.add(animation)
        animation.onfinish = () => animations.delete(animation)
      }
      const revealTargets = shell.querySelectorAll<HTMLElement>('.section-header, .catalog-tools, .product-card, .story-copy, .newsletter > div, .newsletter > .button, .footer-top')
      const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return
          const element = entry.target as HTMLElement
          if (!element.dataset.revealed) {
            element.dataset.revealed = 'true'
            const siblings = element.parentElement ? Array.from(element.parentElement.children) : []
            const delay = element.matches('.product-card') ? (siblings.indexOf(element) % 3) * 65 : 0
            animate(element, [
              { opacity: 0, translate: '0 24px' },
              { opacity: 1, translate: '0 0' },
            ], { duration: 650, delay, easing: 'cubic-bezier(.22,1,.36,1)', fill: 'backwards' })
          }
          observer.unobserve(element)
        })
      }, { threshold: 0.12 })
      revealTargets.forEach((element) => observer.observe(element))

      const hero = shell.querySelector<HTMLElement>('.hero-visual')
      const story = shell.querySelector<HTMLElement>('.story-image')
      const heroImage = hero?.querySelector('img')
      const storyImage = story?.querySelector('img')
      let frame = 0
      let pointerX = 0
      let pointerY = 0
      const render = () => {
        frame = 0
        const viewport = window.innerHeight
        const scrollRange = document.documentElement.scrollHeight - viewport
        shell.style.setProperty('--scroll-progress', String(scrollRange > 0 ? Math.min(1, Math.max(0, window.scrollY / scrollRange)) : 0))
        if (hero && heroImage) {
          const bounds = hero.getBoundingClientRect()
          if (bounds.bottom > 0 && bounds.top < viewport) {
            const offset = Math.max(-35, Math.min(35, -bounds.top * 0.09))
            heroImage.style.setProperty('--parallax-y', `${offset + pointerY}px`)
            heroImage.style.setProperty('--parallax-x', `${pointerX}px`)
          }
        }
        if (story && storyImage) {
          const bounds = story.getBoundingClientRect()
          if (bounds.bottom > 0 && bounds.top < viewport) {
            const offset = Math.max(-30, Math.min(30, (viewport / 2 - bounds.top - bounds.height / 2) * 0.08))
            storyImage.style.setProperty('--parallax-y', `${offset}px`)
          }
        }
      }
      const schedule = () => { if (!frame) frame = requestAnimationFrame(render) }
      const move = (event: PointerEvent) => {
        if (event.pointerType !== 'mouse' || !hero) return
        const bounds = hero.getBoundingClientRect()
        pointerX = (0.5 - (event.clientX - bounds.left) / bounds.width) * 12
        pointerY = (0.5 - (event.clientY - bounds.top) / bounds.height) * 12
        schedule()
      }
      const reset = () => { pointerX = 0; pointerY = 0; schedule() }
      const feedback = (event: MouseEvent) => {
        if (!(event.target instanceof Element)) return
        const button = event.target.closest('.product-bottom button, .favorite-button')
        if (!button) return
        const icon = button.querySelector('svg')
        if (icon) animate(icon, [
          { transform: 'scale(1)' },
          { transform: 'scale(1.3)' },
          { transform: 'scale(1)' },
        ], { duration: 350, easing: 'ease-out' })
        if (button.matches('.product-bottom button')) {
          const bag = shell.querySelector('.bag-button')
          if (bag) animate(bag, [
            { transform: 'rotate(0)' },
            { transform: 'rotate(-12deg)' },
            { transform: 'rotate(10deg)' },
            { transform: 'rotate(0)' },
          ], { duration: 450, easing: 'ease-out' })
        }
      }
      shell.classList.add('motion-enabled')
      window.addEventListener('scroll', schedule, { passive: true })
      window.addEventListener('resize', schedule)
      hero?.addEventListener('pointermove', move)
      hero?.addEventListener('pointerleave', reset)
      shell.addEventListener('click', feedback)
      render()
      dispose = () => {
        cancelAnimationFrame(frame)
        observer.disconnect()
        animations.forEach((animation) => animation.cancel())
        window.removeEventListener('scroll', schedule)
        window.removeEventListener('resize', schedule)
        hero?.removeEventListener('pointermove', move)
        hero?.removeEventListener('pointerleave', reset)
        shell.removeEventListener('click', feedback)
        shell.classList.remove('motion-enabled')
        shell.style.removeProperty('--scroll-progress')
        ;[heroImage, storyImage].forEach((element) => {
          element?.style.removeProperty('--parallax-y')
          element?.style.removeProperty('--parallax-x')
        })
      }
    }
    setup()
    preference.addEventListener('change', setup)
    return () => { dispose(); preference.removeEventListener('change', setup) }
  }, [view, catalogKey])
}

function App() {
  const [showIntro, setShowIntro] = useState(() => !window.matchMedia('(prefers-reduced-motion: reduce)').matches)
  const [products, setProducts] = useState<Product[]>(() => getStored('da-products', initialProducts))
  const [users, setUsers] = useState<User[]>(() => getStored('da-users', []))
  const [currentEmail, setCurrentEmail] = useState<string | null>(() => getStored('da-session', null))
  const [orders, setOrders] = useState<Order[]>(() => getStored('da-orders', []))
  const [cart, setCart] = useState<CartItem[]>(() => getStored('da-cart', []))
  const [favorites, setFavorites] = useState<string[]>(() => getStored('da-favorites', []))
  const [resumo, setResumo] = useState<{ faturamentoTotal: number; pedidosRecebidos: number; produtosAtivos: number; clientesCadastrados: number } | null>(null)
  const [view, setView] = useState<'shop' | 'admin' | 'orders'>('shop')
  const [adminTab, setAdminTab] = useState<'overview' | 'orders' | 'products'>('overview')
  const [category, setCategory] = useState('Todos')
  const [search, setSearch] = useState('')
  const [showSearch, setShowSearch] = useState(false)
  const [cartOpen, setCartOpen] = useState(false)
  const [authOpen, setAuthOpen] = useState(false)
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login')
  const [authRole, setAuthRole] = useState<'customer' | 'admin'>('customer')
  const [authError, setAuthError] = useState('')
  const [authForm, setAuthForm] = useState({ name: '', email: '', password: '', code: '' })
  const [mobileMenu, setMobileMenu] = useState(false)
  const [toast, setToast] = useState('')
  const [mixerFeedback, setMixerFeedback] = useState<{ title: string; message: string } | null>(null)
  const [editingProduct, setEditingProduct] = useState<Product | null>(null)
  const [productForm, setProductForm] = useState({ name: '', category: 'Bolos', description: '', price: '', image: '', stock: '' })

  const user = users.find((item) => item.email === currentEmail)
  const activeProducts = products.filter((item) => item.active)
  const filteredProducts = activeProducts.filter((item) => (category === 'Todos' || item.category === category) && `${item.name} ${item.description}`.toLowerCase().includes(search.toLowerCase()))
  const cartDetails = cart.map((item) => ({ ...item, product: products.find((product) => product.id === item.id) })).filter((item): item is CartItem & { product: Product } => !!item.product)
  const cartCount = cart.reduce((total, item) => total + item.quantity, 0)
  const cartTotal = cartDetails.reduce((total, item) => total + item.quantity * item.product.price, 0)
  const myOrders = orders.filter((order) => order.email === currentEmail)
  const revenue = useMemo(() => orders.reduce((total, order) => total + order.total, 0), [orders])
  useStorefrontMotion(view, filteredProducts.map((product) => product.id).join(','))

  useEffect(() => {
    if (!showIntro) return
    const dismiss = () => setShowIntro(false)
    const preference = window.matchMedia('(prefers-reduced-motion: reduce)')
    const onPreferenceChange = () => { if (preference.matches) dismiss() }
    // A fallback guarantees the decorative curtain never outlives its animation.
    const timeout = window.setTimeout(dismiss, 2700)
    window.addEventListener('pointerdown', dismiss, { once: true })
    window.addEventListener('keydown', dismiss, { once: true })
    preference.addEventListener('change', onPreferenceChange)
    return () => {
      window.clearTimeout(timeout)
      window.removeEventListener('pointerdown', dismiss)
      window.removeEventListener('keydown', dismiss)
      preference.removeEventListener('change', onPreferenceChange)
    }
  }, [showIntro])

  useEffect(() => {
    if (!mixerFeedback) return
    const preference = window.matchMedia('(prefers-reduced-motion: reduce)')
    const finish = () => { setMixerFeedback(null); setToast(mixerFeedback.message) }
    const onKeyDown = (event: KeyboardEvent) => { if (event.key === 'Escape') finish() }
    const onPreferenceChange = () => { if (preference.matches) finish() }
    // Local data is already saved; this is a short success transition, not a network request.
    const timeout = window.setTimeout(finish, preference.matches ? 350 : 1800)
    window.addEventListener('keydown', onKeyDown)
    preference.addEventListener('change', onPreferenceChange)
    return () => {
      window.clearTimeout(timeout)
      window.removeEventListener('keydown', onKeyDown)
      preference.removeEventListener('change', onPreferenceChange)
    }
  }, [mixerFeedback])

  // Acompanha o status dos pedidos na API de vendas (ela avança o status sozinha).
  // Consulta a cada 3s enquanto houver pedido que ainda não foi entregue e avisa
  // com uma notificação quando algum pedido mudar de status.
  const ordersRef = useRef(orders)
  ordersRef.current = orders
  const temPedidoAberto = orders.some((order) => order.status !== 'Entregue')
  useEffect(() => {
    if (!api.enabled || !user || !temPedidoAberto) return
    const role = user.role
    const timer = window.setInterval(async () => {
      const novos = await api.pedidos(role)
      if (!novos) return
      const mudou = novos.find((novo) => {
        const antigo = ordersRef.current.find((order) => order.id === novo.id)
        return antigo && antigo.status !== novo.status
      })
      if (mudou) {
        const mensagens: Record<Order['status'], string> = {
          'Aguardando confirmação': 'Pedido aguardando confirmação.',
          'Confirmado': 'Seu pedido foi confirmado! 🎉',
          'Em rota': 'Seu pedido saiu para entrega.',
          'Entregue': 'Pedido entregue. Bom apetite! 🍫',
        }
        setToast(mensagens[mudou.status])
      }
      setOrders(novos)
    }, 3000)
    return () => window.clearInterval(timer)
  }, [user?.role, temPedidoAberto])
  useEffect(() => { localStorage.setItem('da-products', JSON.stringify(products)) }, [products])
  useEffect(() => { localStorage.setItem('da-users', JSON.stringify(users)) }, [users])
  useEffect(() => { localStorage.setItem('da-session', JSON.stringify(currentEmail)) }, [currentEmail])
  useEffect(() => { localStorage.setItem('da-orders', JSON.stringify(orders)) }, [orders])
  useEffect(() => { localStorage.setItem('da-cart', JSON.stringify(cart)) }, [cart])
  useEffect(() => { localStorage.setItem('da-favorites', JSON.stringify(favorites)) }, [favorites])
  useEffect(() => { if (toast) { const timeout = setTimeout(() => setToast(''), 3200); return () => clearTimeout(timeout) } }, [toast])
  useEffect(() => { document.body.style.overflow = cartOpen || authOpen || !!editingProduct || !!mixerFeedback ? 'hidden' : ''; return () => { document.body.style.overflow = '' } }, [cartOpen, authOpen, editingProduct, mixerFeedback])

  useEffect(() => {
    if (!api.enabled) return
    api.carregar(user?.role).then((data) => { if (data?.products.length) setProducts(data.products); if (data) setOrders(data.orders) })
    if (user?.role === 'admin') api.resumo().then((r) => setResumo(r))
    else setResumo(null)
  }, [currentEmail])

  // Ao recarregar a página, confirma com o backend se o cookie de sessão
  // ainda é válido (sem isso, o front "lembraria" de um login que o
  // servidor já não reconhece mais).
  useEffect(() => {
    if (!api.enabled || !currentEmail) return
    api.me().then((me) => {
      if (!me) { setCurrentEmail(null); return }
      setUsers((items) => [...items.filter((item) => item.email !== me.email), { name: me.name, email: me.email, password: '', role: me.role }])
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function showMixer(title: string, message: string) {
    setShowIntro(false)
    setToast('')
    setMixerFeedback({ title, message })
  }

  function addToCart(id: string) {
    setCart((items) => items.some((item) => item.id === id) ? items.map((item) => item.id === id ? { ...item, quantity: item.quantity + 1 } : item) : [...items, { id, quantity: 1 }])
    setToast('Delícia adicionada à sacola!')
  }
  function updateQuantity(id: string, change: number) { setCart((items) => items.map((item) => item.id === id ? { ...item, quantity: item.quantity + change } : item).filter((item) => item.quantity > 0)) }
  function navigate(next: 'shop' | 'admin' | 'orders') { setView(next); setMobileMenu(false); window.scrollTo({ top: 0, behavior: 'smooth' }) }
  function scrollToSection(id: string) { navigate('shop'); setTimeout(() => document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' }), 40) }
  function openAuth(mode: 'login' | 'register' = 'login') { setAuthMode(mode); setAuthError(''); setAuthForm({ name: '', email: '', password: '', code: '' }); setAuthOpen(true); setMobileMenu(false) }
  function logout() { if (api.enabled) void api.logout(); setCurrentEmail(null); navigate('shop'); setToast('Você saiu da sua conta.') }
  async function remoteAuth(email: string) { // login/cadastro pelo gateway Spring
    try {
      const res = authMode === 'register' ? await api.register({ name: authForm.name.trim(), email, password: authForm.password, role: authRole, code: authForm.code }) : await api.login(email, authForm.password)
      setUsers((items) => [...items.filter((item) => item.email !== email), { name: res.name, email, password: '', role: res.role }])
      setCurrentEmail(email); setAuthOpen(false)
      showMixer(authMode === 'register' ? 'Seu cantinho está pronto.' : 'Que bom ter você de volta.', `Que bom ter você${authMode === 'register' ? '' : ' de volta'}, ${res.name.split(' ')[0]}!`)
      if (res.role === 'admin') navigate('admin')
    } catch (error) { setAuthError(api.errorMessage(error)) }
  }
  function submitAuth(event: React.FormEvent) {
    event.preventDefault()
    if (mixerFeedback) return
    const email = authForm.email.trim().toLowerCase()
    if (api.enabled) { void remoteAuth(email); return }
    if (authMode === 'register') {
      if (users.some((item) => item.email === email)) { setAuthError('Este e-mail já está cadastrado. Faça login.'); return }
      if (authRole === 'admin' && authForm.code !== 'DOCE2025') { setAuthError('Código de acesso administrativo incorreto.'); return }
      setUsers((items) => [...items, { name: authForm.name.trim(), email, password: authForm.password, role: authRole }])
      setCurrentEmail(email)
      showMixer('Seu cantinho está pronto.', 'Conta criada com sucesso. Seja bem-vindo(a)!')
      if (authRole === 'admin') navigate('admin')
    } else {
      const match = users.find((item) => item.email === email && item.password === authForm.password)
      if (!match) { setAuthError('E-mail ou senha incorretos. Tente novamente.'); return }
      setCurrentEmail(email)
      showMixer('Que bom ter você de volta.', `Que bom ter você de volta, ${match.name.split(' ')[0]}!`)
      if (match.role === 'admin') navigate('admin')
    }
    setAuthOpen(false)
  }
  // SOMENTE modo demonstração (sem backend): simula a rotação de status.
  // Com o backend ligado, quem controla o status é a API de vendas e o front
  // apenas consulta (ver o useEffect de acompanhamento dos pedidos).
  function simulateStatusProgress(orderId: string) {
    const steps: { status: Order['status']; delay: number; message: string }[] = [
      { status: 'Confirmado', delay: 4000, message: 'Seu pedido foi confirmado! 🎉' },
      { status: 'Em rota', delay: 9000, message: 'Seu pedido saiu para entrega.' },
      { status: 'Entregue', delay: 14000, message: 'Pedido entregue. Bom apetite! 🍫' },
    ]
    steps.forEach(({ status, delay, message }) => {
      window.setTimeout(() => {
        setOrders((items) => items.map((item) => item.id === orderId ? { ...item, status } : item))
        setToast(message)
      }, delay)
    })
  }
  async function checkout() {
    if (mixerFeedback) return
    if (!user) { setCartOpen(false); openAuth(); setToast('Entre na sua conta para finalizar o pedido.'); return }
    if (!cartDetails.length) return
    if (api.enabled) {
      // O backend calcula o total e desconta o estoque - o front só manda
      // produto + quantidade e usa de volta o que o servidor confirmou.
      const criado = await api.criarPedido(cartDetails.map(({ product, quantity }) => ({ produtoId: product.id, quantidade: quantity })))
      if (!criado) { setToast('Não conseguimos registrar o pedido agora. Tente novamente.'); return }
      // o status inicial (PENDENTE) vem da API; daqui pra frente ela mesma avança o pedido
      setOrders((items) => [criado, ...items]); setCart([]); setCartOpen(false); navigate('orders')
      showMixer('Pedido recebido com carinho.', 'Pedido recebido! Acompanhe os detalhes aqui.')
      return
    }
    const newOrder: Order = { id: `DA-${Date.now().toString().slice(-6)}`, customer: user.name, email: user.email, items: cartDetails.map(({ product, quantity }) => ({ name: product.name, quantity, price: product.price })), total: cartTotal, date: new Date().toISOString(), status: 'Aguardando confirmação' }
    setOrders((items) => [newOrder, ...items]); setCart([]); setCartOpen(false); navigate('orders')
    simulateStatusProgress(newOrder.id)
    showMixer('Pedido recebido com carinho.', 'Pedido recebido! Acompanhe os detalhes aqui.')
  }
  async function saveProduct(event: React.FormEvent) {
    event.preventDefault()
    if (mixerFeedback) return
    const data = { name: productForm.name.trim(), category: productForm.category, description: productForm.description.trim(), price: Number(productForm.price), image: productForm.image.trim() || image('photo-1684436060012-db6da717e7c7'), stock: Number(productForm.stock) }
    if (!Number.isFinite(data.price) || data.price <= 0) return
    if (!Number.isFinite(data.stock) || data.stock < 0) return
    if (api.enabled) {
      const salvo = await api.salvarProduto(data, editingProduct?.id || undefined)
      if (!salvo) { setToast('Não conseguimos salvar o produto agora. Tente novamente.'); return }
      setProducts((items) => editingProduct?.id ? items.map((item) => item.id === salvo.id ? salvo : item) : [...items, salvo])
    } else if (editingProduct?.id) setProducts((items) => items.map((item) => item.id === editingProduct.id ? { ...item, ...data } : item))
    else setProducts((items) => [...items, { ...data, id: `local-${Date.now()}`, active: true }])
    setEditingProduct(null); showMixer('Mais uma delícia pronta.', 'Produto salvo com sucesso!')
  }
  function openProduct(product?: Product) {
    setEditingProduct(product || { id: '', name: '', category: 'Bolos', description: '', price: 0, image: '', active: true, stock: 0 })
    setProductForm(product ? { name: product.name, category: product.category, description: product.description, price: String(product.price), image: product.image, stock: String(product.stock) } : { name: '', category: 'Bolos', description: '', price: '', image: '', stock: '' })
  }

  return <><div className="site-shell" inert={!!mixerFeedback}>
    {showIntro && <div className="chocolate-intro" aria-hidden="true" onAnimationEnd={(event) => {
      if (event.target === event.currentTarget) setShowIntro(false)
    }}>
      <div className="chocolate-intro-base"><Brand /><span>Um momento de doçura.</span></div>
      <div className="chocolate-curtain">
        <svg className="chocolate-edge chocolate-edge-top" viewBox="0 0 1440 180" preserveAspectRatio="none">
          <path d="M0 0H1440V52C1405 52 1415 126 1365 126S1320 28 1270 28S1240 172 1180 172S1140 65 1080 65S1035 135 990 135S960 35 905 35S875 160 820 160S780 68 725 68S690 180 635 180S600 42 545 42S510 135 455 135S420 22 365 22S330 164 275 164S245 60 195 60S165 132 110 132S65 45 0 45Z" />
        </svg>
        <div className="chocolate-intro-brand"><Brand light /><span>Feito com carinho. Até na chegada.</span><div className="chocolate-intro-line" /></div>
        <svg className="chocolate-edge chocolate-edge-bottom" viewBox="0 0 1440 180" preserveAspectRatio="none">
          <path d="M0 0H1440V52C1405 52 1415 126 1365 126S1320 28 1270 28S1240 172 1180 172S1140 65 1080 65S1035 135 990 135S960 35 905 35S875 160 820 160S780 68 725 68S690 180 635 180S600 42 545 42S510 135 455 135S420 22 365 22S330 164 275 164S245 60 195 60S165 132 110 132S65 45 0 45Z" />
        </svg>
      </div>
    </div>}
    {view !== 'admin' && <div className="announcement"><span className="announcement-stars">✳</span> Um toque de doçura para os seus dias <span className="announcement-dot">•</span> Feito com carinho, entregue para você <span className="announcement-stars">✳</span></div>}
    {view === 'admin' ? <div className="admin-layout">
      <aside className="admin-sidebar"><button className="unstyled logo-button" onClick={() => navigate('shop')} aria-label="Ir para a loja"><Brand light /></button><div className="sidebar-label">MENU PRINCIPAL</div><nav className="admin-nav">
        <button className={adminTab === 'overview' ? 'active' : ''} onClick={() => setAdminTab('overview')}><Icon name="chart" /> Visão geral</button>
        <button className={adminTab === 'orders' ? 'active' : ''} onClick={() => setAdminTab('orders')}><Icon name="receipt" /> Vendas e pedidos {orders.filter((o) => o.status === 'Aguardando confirmação').length > 0 && <span className="sidebar-count">{orders.filter((o) => o.status === 'Aguardando confirmação').length}</span>}</button>
        <button className={adminTab === 'products' ? 'active' : ''} onClick={() => setAdminTab('products')}><Icon name="box" /> Produtos</button>
      </nav><div className="sidebar-bottom"><button onClick={() => navigate('shop')}><Icon name="arrow" /> Ver loja</button><button onClick={logout}><Icon name="logout" /> Sair da conta</button><div className="sidebar-profile"><div className="avatar">{user?.name.charAt(0).toUpperCase()}</div><div><strong>{user?.name}</strong><small>Administrador(a)</small></div></div></div></aside>
      <main className="admin-main"><header className="admin-header"><div><span className="eyebrow">PAINEL ADMINISTRATIVO</span><h1>{adminTab === 'overview' ? 'Visão geral' : adminTab === 'orders' ? 'Vendas e pedidos' : 'Seus produtos'}</h1><p>Bem-vindo(a) de volta, {user?.name.split(' ')[0]}. Veja o que acontece na sua doceria.</p></div><div className="admin-header-actions"><span className="today-pill">{new Date().toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' })}</span><div className="avatar">{user?.name.charAt(0).toUpperCase()}</div></div></header>
        {adminTab === 'overview' && <><div className="stat-grid"><div className="stat-card"><div className="stat-icon purple"><Icon name="chart" /></div><span>Faturamento total</span><strong>{money(resumo?.faturamentoTotal ?? revenue)}</strong><small>Em todos os pedidos</small></div><div className="stat-card"><div className="stat-icon peach"><Icon name="bag" /></div><span>Pedidos recebidos</span><strong>{resumo?.pedidosRecebidos ?? orders.length}</strong><small>Desde o início</small></div><div className="stat-card"><div className="stat-icon mint"><Icon name="box" /></div><span>Produtos ativos</span><strong>{resumo?.produtosAtivos ?? activeProducts.length}</strong><small>Disponíveis no catálogo</small></div><div className="stat-card"><div className="stat-icon butter"><Icon name="user" /></div><span>Clientes cadastrados</span><strong>{resumo?.clientesCadastrados ?? users.filter((u) => u.role === 'customer').length}</strong><small>Na sua comunidade</small></div></div><div className="admin-content-grid"><div className="admin-panel"><div className="panel-heading"><div><span className="eyebrow">ACOMPANHAMENTO</span><h2>Pedidos recentes</h2></div><button className="text-link" onClick={() => setAdminTab('orders')}>Ver todos <Icon name="arrow" size={16} /></button></div>{orders.length ? <div className="table-wrap"><table><thead><tr><th>PEDIDO</th><th>CLIENTE</th><th>VALOR</th><th>STATUS</th></tr></thead><tbody>{orders.slice(0, 5).map((order) => <tr key={order.id}><td><strong>#{order.id}</strong></td><td>{order.customer}</td><td>{money(order.total)}</td><td><span className={`status status-${statusClass(order.status)}`}>{order.status}</span></td></tr>)}</tbody></table></div> : <EmptyState title="Ainda não há pedidos" text="As primeiras vendas da sua doceria aparecerão aqui." icon="receipt" />}</div><div className="admin-panel quick-panel"><span className="eyebrow">ACESSO RÁPIDO</span><h2>O que vamos fazer hoje?</h2><p>Gerencie sua loja e mantenha tudo prontinho para os próximos pedidos.</p><button onClick={() => { setAdminTab('products'); openProduct() }}><span className="quick-icon"><Icon name="plus" /></span><span>Adicionar produto<small>Uma nova delícia ao catálogo</small></span><Icon name="arrow" size={18} /></button><button onClick={() => setAdminTab('orders')}><span className="quick-icon"><Icon name="receipt" /></span><span>Ver pedidos<small>Acompanhe as suas vendas</small></span><Icon name="arrow" size={18} /></button></div></div></>}
        {adminTab === 'orders' && <div className="admin-panel full-panel"><div className="panel-heading"><div><span className="eyebrow">GESTÃO DE VENDAS</span><h2>Todos os pedidos <span className="count-pill">{orders.length}</span></h2></div></div>{orders.length ? <div className="table-wrap"><table><thead><tr><th>PEDIDO</th><th>CLIENTE</th><th>ITENS</th><th>DATA</th><th>TOTAL</th><th>STATUS</th></tr></thead><tbody>{orders.map((order) => <tr key={order.id}><td><strong>#{order.id}</strong></td><td><strong>{order.customer}</strong><small className="table-sub">{order.email}</small></td><td><span title={order.items.map((i) => `${i.quantity}x ${i.name}`).join(', ')}>{order.items.map((i) => `${i.quantity}x ${i.name}`).join(', ')}</span></td><td>{new Date(order.date).toLocaleDateString('pt-BR')}</td><td><strong>{money(order.total)}</strong></td><td><span className={`status status-${statusClass(order.status)}`}>{order.status}</span></td></tr>)}</tbody></table></div> : <EmptyState title="Nenhuma venda por enquanto" text="Quando alguém finalizar um pedido na loja, ele aparecerá aqui." icon="bag" />}</div>}
        {adminTab === 'products' && <div className="admin-panel full-panel"><div className="panel-heading"><div><span className="eyebrow">SEU CATÁLOGO</span><h2>Produtos <span className="count-pill">{products.length}</span></h2></div><button className="button button-dark" onClick={() => openProduct()}><Icon name="plus" size={18} /> Novo produto</button></div><div className="table-wrap"><table><thead><tr><th>PRODUTO</th><th>CATEGORIA</th><th>PREÇO</th><th>VISIBILIDADE</th><th>AÇÕES</th></tr></thead><tbody>{products.map((product) => <tr key={product.id}><td><div className="table-product"><img src={product.image} alt="" /><strong>{product.name}</strong></div></td><td>{product.category}</td><td><strong>{money(product.price)}</strong></td><td><button className={`visibility ${product.active ? 'visible' : ''}`} onClick={() => { void api.visibilidade(product.id, !product.active); setProducts((items) => items.map((item) => item.id === product.id ? { ...item, active: !item.active } : item)) }}><span />{product.active ? 'Ativo' : 'Oculto'}</button></td><td><button className="table-action" onClick={() => openProduct(product)}>Editar</button></td></tr>)}</tbody></table></div></div>}
      </main>
    </div> : <>
      <header className="site-header"><div className="header-inner"><button className="unstyled logo-button" onClick={() => navigate('shop')} aria-label="Doce Atelier, ir para o início"><Brand /></button><nav className={`main-nav ${mobileMenu ? 'mobile-open' : ''}`} aria-label="Navegação principal"><button onClick={() => { navigate('shop'); window.scrollTo({ top: 0, behavior: 'smooth' }) }}>Início</button><button onClick={() => scrollToSection('catalogo')}>Cardápio</button><button onClick={() => scrollToSection('sobre')}>Nossa história</button>{user && <button onClick={() => navigate('orders')}>Meus pedidos</button>}{user?.role === 'admin' && <button onClick={() => navigate('admin')}>Painel admin</button>}</nav><div className="header-actions"><button className="icon-button search-trigger" onClick={() => { navigate('shop'); setShowSearch(!showSearch); setTimeout(() => document.getElementById('catalogo')?.scrollIntoView({ behavior: 'smooth' }), 40) }} aria-label="Buscar produtos"><Icon name="search" size={21} /></button><button className="icon-button account-button" onClick={() => user ? (user.role === 'admin' ? navigate('admin') : navigate('orders')) : openAuth()} aria-label={user ? 'Minha conta' : 'Entrar na conta'}><Icon name="user" size={21} /></button>{user?.role !== 'admin' && <button className="icon-button bag-button" onClick={() => setCartOpen(true)} aria-label={`Abrir sacola, ${cartCount} itens`}><Icon name="bag" size={21} />{cartCount > 0 && <span className="bag-count">{cartCount}</span>}</button>}<button className="icon-button menu-trigger" onClick={() => setMobileMenu(!mobileMenu)} aria-label="Abrir menu"><Icon name={mobileMenu ? 'close' : 'menu'} size={23} /></button></div></div></header>
      {view === 'shop' ? <main>
        <section className="hero"><div className="hero-copy"><div className="hero-copy-inner"><div className="hero-kicker"><span className="kicker-line" /> FEITO À MÃO, FEITO COM AMOR</div><h1>A vida fica mais <em>doce</em> por aqui<span className="period">.</span></h1><p>Receitas que abraçam, ingredientes que encantam e um carinho que você sente em cada pedacinho.</p><button className="button button-primary hero-button" onClick={() => scrollToSection('catalogo')}>Explorar delícias <Icon name="arrow" size={19} /></button><div className="hero-social-proof"><div className="proof-avatars"><span>AM</span><span>JU</span><span>LU</span><span>+</span></div><div><div className="proof-stars">★★★★★</div><small>Momentos mais doces, todos os dias</small></div></div></div><div className="hero-decor decor-one">✳</div><div className="hero-decor decor-two">✳</div></div><div className="hero-visual"><img src={image('photo-1684436060012-db6da717e7c7', 1500)} alt="Bolo de chocolate artesanal decorado com morangos frescos" /><div className="hero-photo-shade" /><div className="hero-image-label"><span className="label-icon">✳</span><span>Um pequeno pedaço<br /><strong>de felicidade.</strong></span></div><div className="hero-side-text">DOCES QUE CONTAM HISTÓRIAS · DESDE 2020</div></div></section>
        <div className="benefit-strip"><div><Icon name="sparkle" size={23} /><span>Feito artesanalmente</span></div><span className="strip-divider" /><div><Icon name="heart" size={23} /><span>Ingredientes selecionados</span></div><span className="strip-divider" /><div><Icon name="truck" size={24} /><span>Entregue com carinho</span></div></div>
        <section className="catalog section-container" id="catalogo"><div className="section-header"><div><div className="section-kicker"><span>✳</span> NOSSO CARDÁPIO</div><h2>Escolha sua próxima <em>felicidade.</em></h2><p>Pequenas alegrias feitas para tornar qualquer dia especial.</p></div><div className="header-squiggle">✳</div></div><div className="catalog-tools"><div className="category-tabs" role="group" aria-label="Filtrar por categoria">{['Todos', 'Bolos', 'Tortas', 'Docinhos'].map((item) => <button key={item} className={category === item ? 'selected' : ''} onClick={() => setCategory(item)}>{item}</button>)}</div><div className={`catalog-search ${showSearch ? 'expanded' : ''}`}><Icon name="search" size={18} /><input aria-label="Buscar no cardápio" placeholder="Buscar delícias..." value={search} onChange={(e) => setSearch(e.target.value)} /></div></div><div className="product-grid">{filteredProducts.map((product) => <article className="product-card" key={product.id}><div className="product-image-wrap"><img src={product.image} alt={product.name} loading="lazy" />{product.badge && <span className="product-badge">{product.badge}</span>}<button className={`favorite-button ${favorites.includes(product.id) ? 'is-favorite' : ''}`} onClick={() => setFavorites((items) => items.includes(product.id) ? items.filter((id) => id !== product.id) : [...items, product.id])} aria-label={favorites.includes(product.id) ? `Remover ${product.name} dos favoritos` : `Favoritar ${product.name}`}><Icon name="heart" size={19} /></button></div><div className="product-info"><div className="product-category">{product.category.toUpperCase()}</div><h3>{product.name}</h3><p>{product.description}</p><div className="product-bottom"><strong>{money(product.price)}</strong>{user?.role !== 'admin' && <button onClick={() => addToCart(product.id)} aria-label={`Adicionar ${product.name} à sacola`}><Icon name="plus" size={20} /></button>}</div></div></article>)}</div>{filteredProducts.length === 0 && <div className="no-products">Nenhuma delícia encontrada. Experimente outra busca.</div>}</section>
        <section className="story-section" id="sobre"><div className="story-image"><img src={image('photo-1583338917451-face2751d8d5', 1100)} loading="lazy" alt="Seleção de doces artesanais na vitrine de uma confeitaria" /><div className="story-sticker">feito<br />com <span>♥</span></div></div><div className="story-copy"><div className="section-kicker"><span>✳</span> A NOSSA ESSÊNCIA</div><h2>Mais do que doces, <em>memórias para saborear.</em></h2><p>Acreditamos que os melhores momentos da vida merecem um sabor especial. Por isso, cada receita nasce com tempo, cuidado e aquele ingrediente que não pode faltar: amor.</p><p>Da nossa cozinha para a sua mesa, criamos doces artesanais que fazem cada encontro ser ainda mais inesquecível.</p><button className="text-link story-link" onClick={() => scrollToSection('catalogo')}>Conheça nossas delícias <Icon name="arrow" size={18} /></button></div></section>
        <section className="newsletter"><span className="newsletter-star">✳</span><div><div className="section-kicker">UM CONVITE ESPECIAL</div><h2>Todo dia merece<br />um <em>momento doce.</em></h2><p>Escolha o seu favorito e deixe o resto com a gente.</p></div><button className="button button-light" onClick={() => scrollToSection('catalogo')}>Pedir agora <Icon name="arrow" size={19} /></button><span className="newsletter-star right">✳</span></section>
      </main> : <main className="orders-page section-container"><button className="back-link" onClick={() => navigate('shop')}><Icon name="arrow" size={17} /> Voltar para a loja</button><div className="section-kicker"><span>✳</span> SUA CONTA</div><div className="orders-heading"><div><h1>Meus <em>pedidos.</em></h1><p>Um lugar para acompanhar cada momento doce.</p></div><button className="button button-outline" onClick={logout}><Icon name="logout" size={17} /> Sair da conta</button></div>{myOrders.length ? <div className="customer-orders">{myOrders.map((order) => <article className="customer-order" key={order.id}><div className="customer-order-top"><div><span>PEDIDO #{order.id}</span><h3>{new Date(order.date).toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' })}</h3></div><span className={`status status-${statusClass(order.status)}`}>{order.status}</span></div><div className="customer-order-items">{order.items.map((item, index) => <div key={index}><span>{item.quantity}x {item.name}</span><span>{money(item.price * item.quantity)}</span></div>)}</div><div className="customer-order-total"><strong>Total</strong><strong>{money(order.total)}</strong></div></article>)}</div> : <div className="empty-orders"><div className="empty-illustration"><Icon name="bag" size={40} /></div><h2>Seu primeiro pedido está te esperando.</h2><p>Escolha uma delícia no cardápio e comece a criar momentos mais doces.</p><button className="button button-primary" onClick={() => scrollToSection('catalogo')}>Ver cardápio <Icon name="arrow" size={18} /></button></div>}</main>}
      <footer className="footer"><div className="footer-inner"><div className="footer-top"><div className="footer-brand"><Brand light /><p>Doces feitos com alma, para momentos que ficam no coração.</p></div><div className="footer-links"><div><h4>EXPLORE</h4><button onClick={() => navigate('shop')}>Início</button><button onClick={() => scrollToSection('catalogo')}>Cardápio</button><button onClick={() => scrollToSection('sobre')}>Nossa história</button></div><div><h4>SUA CONTA</h4><button onClick={() => user ? navigate('orders') : openAuth()}>Meus pedidos</button><button onClick={() => user ? navigate(user.role === 'admin' ? 'admin' : 'orders') : openAuth()}>Entrar / Cadastrar</button><button onClick={() => user?.role === 'admin' ? navigate('admin') : openAuth()}>Área administrativa</button></div></div><div className="footer-note"><span>✳</span><p>Espalhando doçura<br />por onde passamos.</p></div></div><div className="footer-bottom"><span>© {new Date().getFullYear()} Doce Atelier. Feito com carinho.</span><span>CONFEITARIA ARTESANAL <span className="footer-heart">♥</span></span></div></div></footer>
    </>}

    {cartOpen && user?.role !== 'admin' && <div className="overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) setCartOpen(false) }}><aside className="cart-drawer" role="dialog" aria-modal="true" aria-label="Sua sacola"><div className="drawer-header"><div><span className="eyebrow">SUAS ESCOLHAS</span><h2>Minha sacola <span>({cartCount})</span></h2></div><button className="icon-button" onClick={() => setCartOpen(false)} aria-label="Fechar sacola"><Icon name="close" /></button></div>{cartDetails.length ? <><div className="cart-items">{cartDetails.map(({ product, quantity }) => <div className="cart-item" key={product.id}><img src={product.image} alt="" /><div className="cart-item-info"><h3>{product.name}</h3><span>{money(product.price)}</span><div className="quantity-control"><button onClick={() => updateQuantity(product.id, -1)} aria-label={`Diminuir quantidade de ${product.name}`}><Icon name="minus" size={14} /></button><span>{quantity}</span><button onClick={() => updateQuantity(product.id, 1)} aria-label={`Aumentar quantidade de ${product.name}`}><Icon name="plus" size={14} /></button></div></div><button className="remove-button" onClick={() => setCart((items) => items.filter((item) => item.id !== product.id))} aria-label={`Remover ${product.name}`}><Icon name="trash" size={17} /></button></div>)}</div><div className="cart-footer"><div className="cart-delivery"><Icon name="truck" size={20} /> Feito com carinho para chegar até você</div><div className="cart-total"><span>Subtotal</span><strong>{money(cartTotal)}</strong></div><small>Entrega e pagamento serão combinados após o pedido.</small><button className="button button-primary" onClick={checkout}>Finalizar pedido <Icon name="arrow" size={19} /></button></div></> : <div className="empty-cart"><div className="empty-illustration"><Icon name="bag" size={36} /></div><h3>Sua sacola está vazia</h3><p>Que tal escolher algo gostoso para alegrar o dia?</p><button className="button button-primary" onClick={() => { setCartOpen(false); scrollToSection('catalogo') }}>Explorar cardápio <Icon name="arrow" size={18} /></button></div>}</aside></div>}

    {authOpen && <div className="overlay modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) setAuthOpen(false) }}><div className="auth-modal" role="dialog" aria-modal="true" aria-label={authMode === 'login' ? 'Entrar na conta' : 'Criar conta'}><button className="modal-close icon-button" onClick={() => setAuthOpen(false)} aria-label="Fechar"><Icon name="close" /></button><div className="auth-mark">✳</div><span className="eyebrow">BEM-VINDO(A) AO DOCE ATELIER</span><h2>{authMode === 'login' ? <>Que bom ter você <em>de volta.</em></> : <>Vamos adoçar essa <em>história?</em></>}</h2><p>{authMode === 'login' ? 'Entre na sua conta para acompanhar seus pedidos.' : 'Crie sua conta para começar a aproveitar.'}</p><div className="auth-tabs"><button className={authMode === 'login' ? 'active' : ''} onClick={() => { setAuthMode('login'); setAuthError('') }}>Entrar</button><button className={authMode === 'register' ? 'active' : ''} onClick={() => { setAuthMode('register'); setAuthError('') }}>Criar conta</button></div><form onSubmit={submitAuth}>{authMode === 'register' && <><div className="role-selector"><button type="button" className={authRole === 'customer' ? 'active' : ''} onClick={() => { setAuthRole('customer'); setAuthError('') }}>Sou cliente</button><button type="button" className={authRole === 'admin' ? 'active' : ''} onClick={() => { setAuthRole('admin'); setAuthError('') }}>Sou administrador</button></div><label>Nome completo<input required value={authForm.name} onChange={(e) => setAuthForm({ ...authForm, name: e.target.value })} placeholder="Seu nome" /></label></>}<label>E-mail<input required type="email" value={authForm.email} onChange={(e) => setAuthForm({ ...authForm, email: e.target.value })} placeholder="voce@email.com" /></label><label>Senha<input required type="password" minLength={6} value={authForm.password} onChange={(e) => setAuthForm({ ...authForm, password: e.target.value })} placeholder="Mínimo de 6 caracteres" /></label>{authMode === 'register' && authRole === 'admin' && <label>Código de acesso<input required value={authForm.code} onChange={(e) => setAuthForm({ ...authForm, code: e.target.value })} placeholder="Código administrativo" /><small>Para testar o painel nesta demonstração, use DOCE2025.</small></label>}{authError && <div className="form-error" role="alert">{authError}</div>}<button className="button button-primary auth-submit" type="submit">{authMode === 'login' ? 'Entrar na minha conta' : 'Criar minha conta'} <Icon name="arrow" size={18} /></button></form><div className="auth-footnote"><Icon name="shield" size={15} /> Demonstração local: seus dados ficam apenas neste navegador.</div></div></div>}

    {editingProduct && <div className="overlay modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) setEditingProduct(null) }}><div className="product-modal" role="dialog" aria-modal="true" aria-label={editingProduct.id ? 'Editar produto' : 'Novo produto'}><button className="modal-close icon-button" onClick={() => setEditingProduct(null)} aria-label="Fechar"><Icon name="close" /></button><span className="eyebrow">GESTÃO DE CATÁLOGO</span><h2>{editingProduct.id ? 'Editar produto' : 'Nova delícia'}</h2><p>Preencha os detalhes que vão aparecer no cardápio.</p><form onSubmit={saveProduct}><label>Nome do produto<input required value={productForm.name} onChange={(e) => setProductForm({ ...productForm, name: e.target.value })} placeholder="Ex.: Bolo de cenoura" /></label><div className="form-row"><label>Categoria<select value={productForm.category} onChange={(e) => setProductForm({ ...productForm, category: e.target.value })}><option>Bolos</option><option>Tortas</option><option>Docinhos</option></select></label><label>Preço (R$)<input required type="number" min="0.01" step="0.01" value={productForm.price} onChange={(e) => setProductForm({ ...productForm, price: e.target.value })} placeholder="0,00" /></label></div><label>Estoque disponível<input required type="number" min="0" step="1" value={productForm.stock} onChange={(e) => setProductForm({ ...productForm, stock: e.target.value })} placeholder="0" /></label><label>Descrição<textarea required rows={3} value={productForm.description} onChange={(e) => setProductForm({ ...productForm, description: e.target.value })} placeholder="Conte o que torna essa delícia especial" /></label><label>URL da imagem<input type="url" value={productForm.image} onChange={(e) => setProductForm({ ...productForm, image: e.target.value })} placeholder="https://..." /><small>Se ficar vazio, usaremos uma foto padrão.</small></label><button className="button button-primary auth-submit" type="submit">Salvar produto <Icon name="check" size={18} /></button></form></div></div>}
    {toast && <div className="toast" role="status"><Icon name="check" size={17} /> {toast}</div>}
  </div>
    {mixerFeedback && <div className="mixer-loading" role="status" aria-live="polite" aria-atomic="true">
      <div className="mixer-loading-content">
        <span className="mixer-loading-kicker">DOCE ATELIER · UM TOQUE DE CARINHO</span>
        <div className="mixer-illustration" aria-hidden="true">
          <svg viewBox="0 0 280 250" fill="none">
            <ellipse className="mixer-shadow" cx="140" cy="230" rx="99" ry="9" />
            <circle className="mixer-halo" cx="140" cy="122" r="104" />
            <path className="mixer-body" d="M68 85H102V200H221Q235 200 235 214V221H52V210Q52 199 68 199Z" />
            <path className="mixer-body-highlight" d="M77 97H85V192H77Z" />
            <g className="mixer-head">
              <path className="mixer-body" d="M50 69Q50 42 81 42H184Q210 42 215 65L220 93H53Z" />
              <path className="mixer-body-highlight" d="M71 51H181Q195 51 201 63H68Q61 63 71 51Z" />
              <path className="mixer-trim" d="M53 85H219V96H53Z" />
              <circle className="mixer-dial" cx="77" cy="71" r="9" />
              <path className="mixer-dial-mark" d="M77 65V70" />
              <path className="mixer-neck" d="M175 96H188V119H175Z" />
            </g>
            <g className="mixer-whisk">
              <path d="M181 115V171M181 122C159 137 159 171 181 175C203 171 203 137 181 122ZM181 122C170 139 170 165 181 175C192 165 192 139 181 122Z" />
            </g>
            <path className="mixer-bowl" d="M119 154H241L231 185Q224 207 180 207Q136 207 129 185Z" />
            <ellipse className="mixer-bowl-rim" cx="180" cy="154" rx="61" ry="13" />
            <ellipse className="mixer-batter" cx="180" cy="155" rx="51" ry="9" />
            <ellipse className="mixer-swirl" cx="180" cy="155" rx="36" ry="5" />
            <path className="mixer-bowl-shine" d="M140 172Q143 185 153 188" />
            <g className="mixer-splash mixer-splash-one"><path d="M132 133L128 124M145 127L145 118" /></g>
            <g className="mixer-splash mixer-splash-two"><path d="M223 133L229 126M214 122L216 115" /></g>
            <path className="mixer-sparkle" d="M236 60V72M230 66H242M37 135V145M32 140H42" />
          </svg>
        </div>
        <div className="mixer-loading-title">{mixerFeedback.title}</div>
        <span className="mixer-loading-description">Só um instante para adoçar sua experiência.</span>
        <div className="mixer-loading-dots" aria-hidden="true"><span /><span /><span /></div>
        <span className="mixer-loading-hint">Pressione Esc para continuar</span>
      </div>
    </div>}
  </>
}

function EmptyState({ title, text, icon }: { title: string; text: string; icon: IconName }) { return <div className="admin-empty"><div className="empty-icon"><Icon name={icon} size={27} /></div><h3>{title}</h3><p>{text}</p></div> }

export default App
