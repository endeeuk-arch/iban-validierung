import { useEffect, useState } from 'react'
import './App.css'

// definition for IBANRESULT such as JAVA
type IbanResult = {
  valid: boolean
  iban: string
  bankName: string | null
}

// table definition
type IbanCheck = {
  id: number
  iban: string
  blz: string | null
  valid: boolean
  createdAt: string
}

function App() {
  const [input, setInput] = useState('')
  const [result, setResult] = useState<IbanResult | null>(null)
  const [history, setHistory] = useState<IbanCheck[]>([])
  const [error, setError] = useState<string | null>(null)

  // Load the latest checks from the backend
  const loadHistory = () => {
    fetch('/api/iban')
      .then(r => r.json())
      .then(setHistory)
      .catch(() => setError('Server nicht erreichbar'))
  }

  // Runs once after the first render
  useEffect(loadHistory, [])

  const check = async () => {
    setError(null)
    try {

      // call validate from Backend 
      const response = await fetch('/api/iban/validate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ iban: input }),
      })
      if (!response.ok) {
        throw new Error()
      }

      setResult(await response.json())
      loadHistory()
    } catch {
      setError('Server nicht erreichbar')
    }
  }

  return (
    <main>
      <h1>IBAN-Check</h1>
      <div className="form">
        <input
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="DE89 3704 0044 0532 0130 00"
        />
        <button onClick={check} disabled={!input.trim()}>Check it</button>
      </div>
      {error && <p className="error">{error}</p>}

      {result && (
        <p className={result.valid ? 'ok' : 'error'}>
          {result.valid ? '✓ VALID' : '✗ Not valid'}: {result.iban}
          {result.bankName && ` (${result.bankName})`}
        </p>
      )}

      <h2>Last checks</h2>
      <table>
        <tbody>
          {history.map(c => (
            <tr key={c.id}>
              <td>{c.valid ? '✓' : '✗'}</td>
              <td>{c.iban}</td>
              <td>{new Date(c.createdAt).toLocaleString('de-DE')}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  )
}

export default App