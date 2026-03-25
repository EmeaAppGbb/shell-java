'use client';

import { useEffect, useState } from 'react';

interface User {
  username: string;
  role: string;
  createdAt: string;
}

export function useAuth() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    fetch('/api/auth/me')
      .then((res) => {
        if (res.ok) return res.json();
        return null;
      })
      .then((data) => setUser(data ?? null))
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  return { user, loading, error };
}
