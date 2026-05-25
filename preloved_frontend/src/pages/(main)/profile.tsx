import { useEffect, useState } from "react";
import { apiFetch } from "../../service/api";

type ProfileType = {
  alamat: string;
  noHp: string;
  foto: string;
};

type UserType = {
  email: string;
  role: string;
  profile: ProfileType | null;
};

export default function ProfilePage() {
  const [user, setUser] = useState<UserType | null>(null);

  const [form, setForm] = useState<ProfileType>({
    alamat: "",
    noHp: "",
    foto: "",
  });

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchMe();
  }, []);

  const fetchMe = async () => {
    try {
      const data = await apiFetch("/api/user/me");

      setUser(data);

      // kalau profile ada, isi form
      if (data.profile) {
        setForm(data.profile);
      }
    } catch (err) {
      console.log(err);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (loading) return;
    setLoading(true);

    try {
      await apiFetch("/api/user/me", {
        method: "PUT",
        body: JSON.stringify(form),
      });

      alert("Profile berhasil diupdate");
      fetchMe();
    } catch (err: any) {
      alert(err.message || "Gagal update profile");
    } finally {
      setLoading(false);
    }
  };

  if (!user) return <div className="p-6">Loading...</div>;

  return (
    <div className="max-w-xl mx-auto p-6 space-y-6">
      <h1 className="text-2xl font-bold">Profile</h1>

      <div className="text-sm text-gray-500">
        {user.email} • {user.role}
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="text-sm">Alamat</label>
          <input
            name="alamat"
            value={form.alamat}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            placeholder="Masukkan alamat"
          />
        </div>

        <div>
          <label className="text-sm">No HP</label>
          <input
            name="noHp"
            value={form.noHp}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            placeholder="08xxxx"
          />
        </div>

        <div>
          <label className="text-sm">Foto URL</label>
          <input
            name="foto"
            value={form.foto}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            placeholder="https://..."
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="bg-black text-white px-4 py-2 rounded"
        >
          {loading ? "Saving..." : "Save Profile"}
        </button>
      </form>
    </div>
  );
}