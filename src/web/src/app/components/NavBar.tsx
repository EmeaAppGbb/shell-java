'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '../hooks/useAuth';

export default function NavBar() {
  const { user, loading } = useAuth();
  const router = useRouter();

  async function handleLogout() {
    await fetch('/api/auth/logout', { method: 'POST' });
    router.push('/login');
    router.refresh();
  }

  return (
    <nav className="flex items-center justify-between border-b border-gray-200 bg-white px-6 py-3">
      <Link href="/" className="text-lg font-semibold text-gray-900">
        UserAuth
      </Link>
      <div className="flex items-center gap-4">
        {loading ? null : user ? (
          <>
            <Link href="/profile" className="text-gray-700 hover:text-gray-900">
              Profile
            </Link>
            {user.role === 'admin' && (
              <Link href="/admin" className="text-gray-700 hover:text-gray-900">
                Admin
              </Link>
            )}
            <button
              onClick={handleLogout}
              className="rounded bg-red-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-red-700"
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <Link href="/login" className="text-gray-700 hover:text-gray-900">
              Login
            </Link>
            <Link href="/register" className="rounded bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700">
              Register
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}
