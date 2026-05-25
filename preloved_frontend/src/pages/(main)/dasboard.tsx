import { useEffect, useState } from "react";
import { apiFetch } from "../../service/api";

export default function Dashboard() {

  const [user, setUser] = useState<any>(null);

  useEffect(() => {

    fetchUser();

  }, []);

  const fetchUser = async () => {

    try {

      const data = await apiFetch(
        "/api/auth/me"
      );

      setUser(data);

    } catch (err) {

      console.log(err);
    }
  };

  const logout = async () => {

    await apiFetch(
      "/api/auth/logout",
      {
        method: "POST",
      }
    );

    window.location.href = "/login";
  };

  if (!user) {

    return <h1>Loading...</h1>;
  }

  return (
    <div>

      <h1 className="text-dashboard text-blue-600">Dashboard</h1>

      <p>Email: {user.email}</p>

      <p>Role: {user.role}</p>

      <button onClick={logout}>
        Logout
      </button>

    </div>
  );
}