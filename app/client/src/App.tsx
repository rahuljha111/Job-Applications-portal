import { RouterProvider } from "react-router";
import { router } from "./routes";
import { Toaster } from "./comoponents/ui/sonner";
import { AuthProvider } from "./contexts/AuthContext";

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
      <Toaster />
    </AuthProvider>
  );
}