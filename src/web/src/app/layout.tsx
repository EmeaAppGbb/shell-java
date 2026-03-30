import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "spec2cloud — Java",
  description: "spec2cloud shell — Java",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
