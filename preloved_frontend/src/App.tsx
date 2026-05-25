
import {
  BrowserRouter,
  Routes,
  Route,
} from "react-router-dom";
import Dashboard from './pages/(main)/dasboard';
import Login from './pages/(auth)/Login';
import Register from './pages/(auth)/Register';
import ProfilePage from "./pages/(main)/profile";

function App() {


  return (
    <BrowserRouter>

      <Routes>
        <Route
          path="/"
          element={<Dashboard />} 
          />

        <Route
          path="/login"
          element={<Login />} 
          />

        <Route
          path="/register"
          element={<Register />} 
          />
        <Route
          path="/profile"
          element={<ProfilePage />} 
          />

      </Routes>
    </BrowserRouter>
  )
}

export default App
