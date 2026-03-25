import { Suspense } from 'react';
import LoginForm from '../components/LoginForm';

export default function LoginPage() {
  return (
    <main className="flex min-h-[80vh] items-center justify-center px-4">
      <Suspense>
        <LoginForm />
      </Suspense>
    </main>
  );
}
