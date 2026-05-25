import { useState, useEffect } from "react";
import { apiFetch } from "../../service/api";

const CAROUSEL_IMAGES = [
  {
    url: "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?q=80&w=1200&auto=format&fit=crop",
    title: "Curated Vintage Pieces",
    desc: "Pakaian pilihan berkualitas tinggi yang dikurasi khusus untuk gaya minimalis Anda.",
  },
  {
    url: "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?q=80&w=1200&auto=format&fit=crop",
    title: "Sustainable Wardrobe",
    desc: "Mulai langkah bermakna dengan mendukung perputaran fashion yang ramah lingkungan.",
  },
  {
    url: "https://images.unsplash.com/photo-1485968579580-b6d095142e6e?q=80&w=1200&auto=format&fit=crop",
    title: "Timeless Aesthetics",
    desc: "Temukan gaya klasik dan modern dari berbagai koleksi preloved brand ternama.",
  }
];

export default function Login() {
  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [rememberMe, setRememberMe] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [currentSlide, setCurrentSlide] = useState(0);
  
  // State Baru untuk Loading & Notification Toast
  const [isLoading, setIsLoading] = useState(false);
  const [toast, setToast] = useState<{ show: boolean; message: string; type: "success" | "error" }>({
    show: false,
    message: "",
    type: "success",
  });

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % CAROUSEL_IMAGES.length);
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  // Otomatis menyembunyikan toast setelah 4 detik
  useEffect(() => {
    if (toast.show) {
      const timer = setTimeout(() => {
        setToast((prev) => ({ ...prev, show: false }));
      }, 4000);
      return () => clearTimeout(timer);
    }
  }, [toast.show]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const showCustomAlert = (message: string, type: "success" | "error") => {
    setToast({ show: true, message, type });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isLoading) return;

    setIsLoading(true); // Aktifkan loading spinner

    try {
      const data = await apiFetch("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ ...form, rememberMe }),
      });
      console.log(data);
      
      showCustomAlert("Login Berhasil! Mengalihkan ke dashboard...", "success");
      
      // Delay sedikit agar user bisa melihat animasi alert sukses
      setTimeout(() => {
        window.location.href = "/dashboard";
      }, 1500);

    } catch (err: any) {
      showCustomAlert(err.message || "Terjadi kesalahan saat login.", "error");
    } finally {
      setIsLoading(false); // Matikan loading spinner
    }
  };

  return (
    <div className="flex h-screen w-screen bg-white text-zinc-900 font-sans overflow-hidden relative">
      
      {/* --- TOAST NOTIFICATION KUSTOM --- */}
      <div className={`absolute top-6 right-6 z-50 flex items-center gap-3 px-4 py-3.5 rounded-xl border shadow-xl transition-all duration-300 max-w-md transform ${
        toast.show ? "translate-y-0 opacity-100 scale-100" : "-translate-y-4 opacity-0 scale-95 pointer-events-none"
      } ${
        toast.type === "success" 
          ? "bg-zinc-900 border-zinc-800 text-white" 
          : "bg-red-50 border-red-200 text-red-900"
      }`}>
        {toast.type === "success" ? (
          <div className="flex h-5 w-5 items-center justify-center rounded-full bg-emerald-500/20 text-emerald-400 text-xs font-bold">✓</div>
        ) : (
          <div className="flex h-5 w-5 items-center justify-center rounded-full bg-red-200 text-red-600 text-xs font-bold">!</div>
        )}
        <p className="text-xs font-medium tracking-wide leading-relaxed">{toast.message}</p>
        <button 
          onClick={() => setToast((prev) => ({ ...prev, show: false }))} 
          className={`text-[10px] font-bold ml-4 tracking-wider hover:opacity-70 ${toast.type === "success" ? "text-zinc-400" : "text-red-500"}`}
        >
          CLOSE
        </button>
      </div>

      {/* --- BAGIAN KIRI: FULL SCREEN CAROUSEL --- */}
      <div className="hidden md:flex md:w-1/2 h-full relative flex-col justify-end p-12 overflow-hidden bg-zinc-50">
        {CAROUSEL_IMAGES.map((img, index) => (
          <div
            key={index}
            className={`absolute inset-0 bg-cover bg-center transition-opacity duration-1000 ease-in-out ${
              index === currentSlide ? "opacity-90 scale-100" : "opacity-0 scale-105"
            }`}
            style={{ backgroundImage: `url('${img.url}')` }}
          />
        ))}
        
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-black/10" />
        
        <div className="absolute top-10 left-12 flex items-center gap-2.5 z-10">
          <span className="text-xl font-bold tracking-tight text-white drop-shadow-sm">
            preloved<span className="font-light text-zinc-200">vibe.</span>
          </span>
        </div>
        
        <div className="relative z-10 space-y-3 max-w-lg mb-6 text-white">
          <h2 className="text-3xl font-semibold tracking-tight drop-shadow-sm leading-tight">
            {CAROUSEL_IMAGES[currentSlide].title}
          </h2>
          <p className="text-sm text-zinc-200/90 leading-relaxed drop-shadow-sm">
            {CAROUSEL_IMAGES[currentSlide].desc}
          </p>

          <div className="flex gap-2 pt-4">
            {CAROUSEL_IMAGES.map((_, index) => (
              <button
                key={index}
                onClick={() => setCurrentSlide(index)}
                className={`h-1 rounded-full transition-all duration-300 ${
                  index === currentSlide ? "w-8 bg-white" : "w-2 bg-white/40"
                }`}
                aria-label={`Go to slide ${index + 1}`}
              />
            ))}
          </div>
        </div>
      </div>

      {/* --- BAGIAN KANAN: FORM LOGIN --- */}
      <div className="w-full md:w-1/2 h-full bg-white flex flex-col justify-between p-8 sm:p-16 relative border-l border-zinc-100">
        
        <div className="flex justify-between md:justify-end items-center">
          <div className="flex items-center gap-2 md:hidden">
            <span className="text-lg font-bold tracking-tight text-zinc-900">
              preloved<span className="font-light text-zinc-500">vibe.</span>
            </span>
          </div>
          <button
            onClick={() => window.location.href = "/register"}
            disabled={isLoading}
            className="text-xs font-medium text-zinc-600 hover:text-zinc-900 border border-zinc-200 hover:border-zinc-300 bg-white px-4 py-2 rounded-full transition-all shadow-sm disabled:opacity-50"
          >
            Sign up
          </button>
        </div>

        <div className="w-full max-w-sm mx-auto my-auto space-y-7">
          <div className="space-y-1">
            <h1 className="text-2xl sm:text-3xl font-semibold text-zinc-900 tracking-tight">
              Welcome Back
            </h1>
            <p className="text-xs sm:text-sm text-zinc-400">
              Silakan masuk untuk mengelola katalog pakaianmu.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Input Email */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-zinc-500 tracking-wide">
                Email Address
              </label>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="name@example.com"
                required
                disabled={isLoading}
                className="w-full bg-zinc-50/50 border border-zinc-200 focus:border-zinc-400 text-zinc-900 rounded-xl px-4 py-3 text-sm transition-all outline-none placeholder:text-zinc-400 focus:ring-1 focus:ring-zinc-400 disabled:opacity-60"
              />
            </div>

            {/* Input Password */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-zinc-500 tracking-wide">
                Password
              </label>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  required
                  disabled={isLoading}
                  className="w-full bg-zinc-50/50 border border-zinc-200 focus:border-zinc-400 text-zinc-900 rounded-xl pl-4 pr-12 py-3 text-sm transition-all outline-none placeholder:text-zinc-400 focus:ring-1 focus:ring-zinc-400 disabled:opacity-60"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-4 flex items-center text-xs font-medium text-zinc-400 hover:text-zinc-700 transition-colors select-none"
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>
            </div>

            {/* Options */}
            <div className="flex items-center justify-between text-xs pt-0.5">
              <label className="flex items-center gap-2 cursor-pointer select-none text-zinc-500 hover:text-zinc-800">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  disabled={isLoading}
                  className="w-4 h-4 rounded border-zinc-300 bg-white text-zinc-900 focus:ring-0 focus:ring-offset-0 accent-zinc-900 disabled:opacity-50"
                />
                Remember me
              </label>
              <a href="/forgot-password" className="text-zinc-500 hover:text-zinc-900 font-medium transition-colors">
                Forgot password?
              </a>
            </div>

            {/* Action Button dengan Integrasi Loading */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-zinc-900 hover:bg-zinc-800 text-white font-medium text-sm py-3 px-4 rounded-xl shadow-sm transition-all active:scale-[0.99] mt-3 tracking-wide flex items-center justify-center gap-2 disabled:bg-zinc-700 disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <>
                  {/* Minimalist Spinner */}
                  <svg className="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  <span>Signing in...</span>
                </>
              ) : (
                <span>Sign in</span>
              )}
            </button>
          </form>
        </div>

        <div className="text-center text-xs text-zinc-400">
          Don't have an account?{" "}
          <a href="/register" className="text-zinc-900 hover:underline font-medium">
            Register for free
          </a>
        </div>

      </div>
    </div>
  );
}